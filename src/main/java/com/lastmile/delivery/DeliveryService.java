package com.lastmile.delivery;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.lastmile.security.AppUserDetails;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import com.lastmile.user.UserAccountRepository;
import com.lastmile.web.BusinessRuleException;
import com.lastmile.web.ConflictException;
import com.lastmile.web.NotFoundException;
import com.lastmile.workflow.DeliveryStatusHistory;
import com.lastmile.workflow.DeliveryStatusHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Delivery business rules. Every public method takes the authenticated principal and re-checks
 * role and ownership itself, so controllers cannot bypass authorization by mistake. Every status
 * change and its history row are written in the same transaction.
 */
@Service
public class DeliveryService {

    private final DeliveryRepository deliveries;
    private final DeliveryStatusHistoryRepository history;
    private final UserAccountRepository users;
    private final DeliveryCodeGenerator codeGenerator;
    private final Clock clock;

    public DeliveryService(DeliveryRepository deliveries, DeliveryStatusHistoryRepository history,
                           UserAccountRepository users, DeliveryCodeGenerator codeGenerator, Clock clock) {
        this.deliveries = deliveries;
        this.history = history;
        this.users = users;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    /**
     * Creates a delivery in status CREATED with its initial history row. If an agent is given, the
     * CREATED -> ASSIGNED transition is applied in the same transaction with its own history row.
     */
    @Transactional
    public DeliveryView create(DeliveryForm form, AppUserDetails principal) {
        UserAccount actor = requireStaff(principal);
        UserAccount agent = form.getAssignedAgentId() == null ? null
                : requireAssignableAgent(form.getAssignedAgentId(), "assignedAgentId");

        Instant now = clock.instant();
        Delivery delivery = new Delivery(codeGenerator.next(now), form.toDetails(), actor, now);
        deliveries.save(delivery);
        record(delivery, null, DeliveryStatus.CREATED, null, actor, now);

        if (agent != null) {
            delivery.assignAgent(agent, now);
            delivery.applyTransition(DeliveryStatus.ASSIGNED, null, now);
            record(delivery, DeliveryStatus.CREATED, DeliveryStatus.ASSIGNED, "Assigned to " + agent.getFullName(), actor, now);
        }
        return toView(delivery);
    }

    @Transactional(readOnly = true)
    public DeliveryView get(long id, AppUserDetails principal) {
        requireActiveUser(principal);
        return toView(findVisible(id, principal));
    }

    /** Agents are always restricted to their own deliveries, whatever agent filter was requested. */
    @Transactional(readOnly = true)
    public Page<DeliverySummary> search(DeliverySearch criteria, int page, int size, AppUserDetails principal) {
        UserAccount actor = requireActiveUser(principal);
        DeliverySearch scoped = actor.getRole() == Role.AGENT ? criteria.forAgent(actor.getId()) : criteria;
        int boundedSize = Math.clamp(size, 1, DeliverySearch.MAX_PAGE_SIZE);
        var pageable = PageRequest.of(Math.max(page, 0), boundedSize,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        return deliveries.findAll(scoped.toSpecification(), pageable).map(DeliverySummary::from);
    }

    /** Edits business fields only; status, assignment and history are untouched. */
    @Transactional
    public DeliveryView update(long id, DeliveryForm form, Long expectedVersion, AppUserDetails principal) {
        UserAccount actor = requireStaff(principal);
        Delivery delivery = findVisible(id, principal);
        checkVersion(delivery, expectedVersion);
        if (!DeliveryPolicy.canEdit(actor.getRole(), delivery.getStatus())) {
            throw new ConflictException("A " + delivery.getStatus().label().toLowerCase() + " delivery cannot be edited.");
        }
        delivery.updateDetails(form.toDetails(), clock.instant());
        return toView(delivery);
    }

    /**
     * Assigns or reassigns an active agent (decision D7). From CREATED or FAILED this is the
     * transition to ASSIGNED; while ASSIGNED it changes the agent and records an audit row.
     */
    @Transactional
    public DeliveryView assign(long id, Long agentId, Long expectedVersion, AppUserDetails principal) {
        UserAccount actor = requireStaff(principal);
        Delivery delivery = findVisible(id, principal);
        checkVersion(delivery, expectedVersion);
        DeliveryStatus current = delivery.getStatus();
        if (!DeliveryPolicy.canAssign(actor.getRole(), current)) {
            throw new ConflictException("A delivery that is " + current.label().toLowerCase() + " cannot be reassigned.");
        }
        if (agentId == null) {
            throw new BusinessRuleException("agentId", "Choose an active delivery agent.");
        }
        UserAccount agent = requireAssignableAgent(agentId, "agentId");
        Instant now = clock.instant();

        if (current == DeliveryStatus.ASSIGNED) {
            if (delivery.getAssignedAgent() != null && delivery.getAssignedAgent().getId().equals(agent.getId())) {
                throw new BusinessRuleException("agentId", "The delivery is already assigned to " + agent.getFullName() + ".");
            }
            delivery.assignAgent(agent, now);
            record(delivery, DeliveryStatus.ASSIGNED, DeliveryStatus.ASSIGNED, "Reassigned to " + agent.getFullName(), actor, now);
        } else {
            String reason = (current == DeliveryStatus.FAILED ? "Retry assigned to " : "Assigned to ") + agent.getFullName();
            delivery.assignAgent(agent, now);
            delivery.applyTransition(DeliveryStatus.ASSIGNED, null, now);
            record(delivery, current, DeliveryStatus.ASSIGNED, reason, actor, now);
        }
        return toView(delivery);
    }

    /**
     * Applies a requested status change after checking ownership, the state machine and the
     * reason rule. Invalid requests change nothing.
     */
    @Transactional
    public DeliveryView changeStatus(long id, DeliveryStatus target, String reason, Long expectedVersion,
                                     AppUserDetails principal) {
        UserAccount actor = requireActiveUser(principal);
        Delivery delivery = findVisible(id, principal);
        checkVersion(delivery, expectedVersion);
        if (target == null) {
            throw new BusinessRuleException("newStatus", "Choose a status.");
        }
        DeliveryStatus current = delivery.getStatus();
        if (!DeliveryPolicy.canChangeStatus(actor.getRole(), current, target)) {
            String hint = target == DeliveryStatus.ASSIGNED ? " Use assignment to choose an agent." : "";
            throw new ConflictException("Cannot change status from " + current.label() + " to " + target.label() + "." + hint);
        }
        String trimmedReason = reason == null || reason.isBlank() ? null : reason.trim();
        if (target == DeliveryStatus.FAILED && trimmedReason == null) {
            throw new BusinessRuleException("reason", "A reason is required when a delivery fails.");
        }
        if (trimmedReason != null && trimmedReason.length() > 500) {
            throw new BusinessRuleException("reason", "Reason must be at most 500 characters.");
        }
        Instant now = clock.instant();
        delivery.applyTransition(target, trimmedReason, now);
        record(delivery, current, target, trimmedReason, actor, now);
        return toView(delivery);
    }

    @Transactional(readOnly = true)
    public DeliveryPolicy.Actions actionsFor(DeliveryView view, AppUserDetails principal) {
        return DeliveryPolicy.actionsFor(principal.getRole(), view.status());
    }

    @Transactional(readOnly = true)
    public List<AgentOption> assignableAgents() {
        return users.findByRoleAndActiveTrueOrderByFullNameAsc(Role.AGENT).stream()
                .map(u -> new AgentOption(u.getId(), u.getFullName()))
                .toList();
    }

    public record AgentOption(long id, String name) {
    }

    /** Agents only see deliveries assigned to them; anything else is reported as not found. */
    Delivery findVisible(long id, AppUserDetails principal) {
        Delivery delivery = deliveries.findById(id).orElseThrow(() -> new NotFoundException("Delivery not found"));
        if (principal.getRole() == Role.AGENT) {
            UserAccount agent = delivery.getAssignedAgent();
            if (agent == null || agent.getId() != principal.getId()) {
                throw new NotFoundException("Delivery not found");
            }
        }
        return delivery;
    }

    UserAccount requireStaff(AppUserDetails principal) {
        UserAccount actor = requireActiveUser(principal);
        if (!actor.getRole().isStaff()) {
            throw new AccessDeniedException("Only administrators and dispatchers may do this");
        }
        return actor;
    }

    UserAccount requireActiveUser(AppUserDetails principal) {
        // Re-read from the database: a session may outlive deactivation or a role change.
        UserAccount actor = users.findById(principal.getId())
                .orElseThrow(() -> new AccessDeniedException("Unknown user"));
        if (!actor.isActive() || actor.getRole() != principal.getRole()) {
            throw new AccessDeniedException("Account is inactive or its role changed; sign in again");
        }
        return actor;
    }

    UserAccount requireAssignableAgent(long agentId, String field) {
        return users.findById(agentId)
                .filter(UserAccount::isActiveAgent)
                .orElseThrow(() -> new BusinessRuleException(field, "Choose an active delivery agent."));
    }

    /**
     * Optional optimistic-concurrency token: forms send the version they displayed, so a second
     * click or another user's change is rejected instead of silently applied twice.
     */
    private static void checkVersion(Delivery delivery, Long expectedVersion) {
        if (expectedVersion != null && expectedVersion != delivery.getVersion()) {
            throw new ConflictException("This delivery was changed by someone else. Reload the page and try again.");
        }
    }

    private void record(Delivery delivery, DeliveryStatus previous, DeliveryStatus next, String reason,
                        UserAccount actor, Instant now) {
        history.save(new DeliveryStatusHistory(delivery, previous, next, reason, actor, now));
    }

    /** Flushes first so the returned view carries the incremented optimistic-lock version. */
    private DeliveryView toView(Delivery delivery) {
        deliveries.flush();
        return DeliveryView.from(delivery, history.findByDeliveryIdOrderByChangedAtAscIdAsc(delivery.getId()));
    }
}
