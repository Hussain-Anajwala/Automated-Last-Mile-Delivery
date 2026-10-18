package com.lastmile.delivery;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.lastmile.security.AppUserDetails;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import com.lastmile.user.UserAccountRepository;
import com.lastmile.web.BusinessRuleException;
import com.lastmile.web.NotFoundException;
import com.lastmile.workflow.DeliveryStatusHistory;
import com.lastmile.workflow.DeliveryStatusHistoryRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Delivery business rules. Every public method takes the authenticated principal and re-checks
 * role and ownership itself, so controllers cannot bypass authorization by mistake.
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
        UserAccount agent = form.getAssignedAgentId() == null ? null : requireAssignableAgent(form.getAssignedAgentId());

        Instant now = clock.instant();
        Delivery delivery = new Delivery(codeGenerator.next(now), form.toDetails(), actor, now);
        deliveries.save(delivery);
        history.save(new DeliveryStatusHistory(delivery, null, DeliveryStatus.CREATED, null, actor, now));

        if (agent != null) {
            delivery.assignAgent(agent, now);
            delivery.applyTransition(DeliveryStatus.ASSIGNED, null, now);
            history.save(new DeliveryStatusHistory(delivery, DeliveryStatus.CREATED, DeliveryStatus.ASSIGNED,
                    "Assigned to " + agent.getFullName(), actor, now));
        }
        return toView(delivery);
    }

    @Transactional(readOnly = true)
    public DeliveryView get(long id, AppUserDetails principal) {
        return toView(findVisible(id, principal));
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
        if (!actor.isActive()) {
            throw new AccessDeniedException("Account is inactive");
        }
        return actor;
    }

    UserAccount requireAssignableAgent(long agentId) {
        return users.findById(agentId)
                .filter(UserAccount::isActiveAgent)
                .orElseThrow(() -> new BusinessRuleException("assignedAgentId", "Choose an active delivery agent."));
    }

    private DeliveryView toView(Delivery delivery) {
        return DeliveryView.from(delivery, history.findByDeliveryIdOrderByChangedAtAscIdAsc(delivery.getId()));
    }
}
