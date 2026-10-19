package com.lastmile.delivery;

import static com.lastmile.delivery.DeliveryStatus.ASSIGNED;
import static com.lastmile.delivery.DeliveryStatus.CREATED;
import static com.lastmile.delivery.DeliveryStatus.DELIVERED;
import static com.lastmile.delivery.DeliveryStatus.FAILED;
import static com.lastmile.delivery.DeliveryStatus.OUT_FOR_DELIVERY;
import static com.lastmile.support.TestData.principal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

import com.lastmile.support.IntegrationTest;
import com.lastmile.support.TestData;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import com.lastmile.web.BusinessRuleException;
import com.lastmile.web.ConflictException;
import com.lastmile.web.NotFoundException;
import com.lastmile.workflow.DeliveryStatusHistory;
import com.lastmile.workflow.DeliveryStatusHistoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@IntegrationTest
class DeliveryWorkflowServiceTest {

    @Autowired DeliveryService service;
    @Autowired TestData data;
    @Autowired JdbcTemplate jdbc;
    @MockitoSpyBean DeliveryStatusHistoryRepository historyRepository;

    UserAccount admin;
    UserAccount dispatcher;
    UserAccount agent;
    UserAccount otherAgent;

    @BeforeEach
    void setUp() {
        data.reset();
        admin = data.user("Ada Admin", Role.ADMIN);
        dispatcher = data.user("Dina Dispatcher", Role.DISPATCHER);
        agent = data.user("Ari Agent", Role.AGENT);
        otherAgent = data.user("Oli Agent", Role.AGENT);
    }

    @AfterEach
    void resetSpy() {
        reset(historyRepository);
    }

    @Test
    void agentCompletesTheHappyPathAndEveryStepIsAuditedWithTheAgentAsActor() {
        long id = data.delivery(dispatcher, agent).id();

        service.changeStatus(id, OUT_FOR_DELIVERY, null, null, principal(agent));
        DeliveryView done = service.changeStatus(id, DELIVERED, null, null, principal(agent));

        assertThat(done.status()).isEqualTo(DELIVERED);
        assertThat(done.deliveredAt()).isNotNull();
        assertThat(done.history()).extracting(DeliveryView.HistoryEntry::newStatus)
                .containsExactly(CREATED, ASSIGNED, OUT_FOR_DELIVERY, DELIVERED);
        assertThat(done.history().subList(2, 4)).allSatisfy(h -> assertThat(h.changedByName()).isEqualTo("Ari Agent"));
    }

    @Test
    void failingRequiresAReasonAndAMissingReasonChangesNothing() {
        long id = data.delivery(dispatcher, agent).id();
        service.changeStatus(id, OUT_FOR_DELIVERY, null, null, principal(agent));
        int historyBefore = data.historyCount(id);

        assertThatThrownBy(() -> service.changeStatus(id, FAILED, "   ", null, principal(agent)))
                .isInstanceOf(BusinessRuleException.class).extracting("field").isEqualTo("reason");
        assertThat(data.status(id)).isEqualTo("OUT_FOR_DELIVERY");
        assertThat(data.historyCount(id)).isEqualTo(historyBefore);

        DeliveryView failed = service.changeStatus(id, FAILED, " Recipient unavailable ", null, principal(agent));
        assertThat(failed.failureReason()).isEqualTo("Recipient unavailable");
        assertThat(failed.history()).last().satisfies(h -> assertThat(h.reason()).isEqualTo("Recipient unavailable"));
    }

    @Test
    void invalidTransitionsAreRejectedWith409AndChangeNothing() {
        long created = data.delivery(dispatcher, null).id();
        assertThatThrownBy(() -> service.changeStatus(created, DELIVERED, null, null, principal(dispatcher)))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> service.changeStatus(created, ASSIGNED, null, null, principal(dispatcher)))
                .isInstanceOf(ConflictException.class).hasMessageContaining("Use assignment");
        assertThat(data.status(created)).isEqualTo("CREATED");
        assertThat(data.historyCount(created)).isEqualTo(1);

        long delivered = data.delivery(dispatcher, agent).id();
        service.changeStatus(delivered, OUT_FOR_DELIVERY, null, null, principal(agent));
        service.changeStatus(delivered, DELIVERED, null, null, principal(agent));
        for (DeliveryStatus target : DeliveryStatus.values()) {
            assertThatThrownBy(() -> service.changeStatus(delivered, target, "x", null, principal(admin)))
                    .as("DELIVERED -> %s", target).isInstanceOf(ConflictException.class);
        }
        assertThat(data.status(delivered)).isEqualTo("DELIVERED");
        assertThatThrownBy(() -> service.assign(delivered, otherAgent.getId(), null, principal(dispatcher)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void agentsCannotSeeOrChangeAnotherAgentsDelivery() {
        long id = data.delivery(dispatcher, agent).id();

        assertThatThrownBy(() -> service.get(id, principal(otherAgent))).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.changeStatus(id, OUT_FOR_DELIVERY, null, null, principal(otherAgent)))
                .isInstanceOf(NotFoundException.class);
        assertThat(data.status(id)).isEqualTo("ASSIGNED");
    }

    @Test
    void agentsCannotEditOrAssign() {
        long id = data.delivery(dispatcher, agent).id();
        assertThatThrownBy(() -> service.assign(id, otherAgent.getId(), null, principal(agent)))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.update(id, new DeliveryForm(), null, principal(agent)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adminFollowsTheSameStateMachineAndIsAudited() {
        long id = data.delivery(dispatcher, agent).id();
        DeliveryView view = service.changeStatus(id, OUT_FOR_DELIVERY, null, null, principal(admin));
        assertThat(view.history()).last().satisfies(h -> assertThat(h.changedByName()).isEqualTo("Ada Admin"));
    }

    @Test
    void retryAfterFailureReassignsClearsTheReasonOnTheRowAndKeepsItInHistory() {
        long id = data.delivery(dispatcher, agent).id();
        service.changeStatus(id, OUT_FOR_DELIVERY, null, null, principal(agent));
        service.changeStatus(id, FAILED, "Address not found", null, principal(agent));

        DeliveryView retried = service.assign(id, otherAgent.getId(), null, principal(dispatcher));

        assertThat(retried.status()).isEqualTo(ASSIGNED);
        assertThat(retried.assignedAgentName()).isEqualTo("Oli Agent");
        assertThat(retried.failureReason()).isNull();
        assertThat(retried.history()).extracting(DeliveryView.HistoryEntry::reason)
                .contains("Address not found", "Retry assigned to Oli Agent");
    }

    @Test
    void reassignmentWhileAssignedIsAuditedButNotAllowedOnceOutForDelivery() {
        long id = data.delivery(dispatcher, agent).id();

        DeliveryView reassigned = service.assign(id, otherAgent.getId(), null, principal(dispatcher));
        assertThat(reassigned.history()).last().satisfies(h -> {
            assertThat(h.previousStatus()).isEqualTo(ASSIGNED);
            assertThat(h.newStatus()).isEqualTo(ASSIGNED);
            assertThat(h.reason()).isEqualTo("Reassigned to Oli Agent");
        });
        assertThatThrownBy(() -> service.assign(id, otherAgent.getId(), null, principal(dispatcher)))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("already assigned");

        service.changeStatus(id, OUT_FOR_DELIVERY, null, null, principal(otherAgent));
        assertThatThrownBy(() -> service.assign(id, agent.getId(), null, principal(dispatcher)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void staleVersionIsRejectedSoADoubleSubmitCannotApplyTwice() {
        DeliveryView view = data.delivery(dispatcher, agent);
        service.changeStatus(view.id(), OUT_FOR_DELIVERY, null, view.version(), principal(agent));

        assertThatThrownBy(() -> service.changeStatus(view.id(), DELIVERED, null, view.version(), principal(agent)))
                .isInstanceOf(ConflictException.class).hasMessageContaining("changed by someone else");
        assertThat(data.status(view.id())).isEqualTo("OUT_FOR_DELIVERY");
    }

    @Test
    void statusAndHistoryAreAtomicWhenTheHistoryInsertFails() {
        long id = data.delivery(dispatcher, agent).id();
        int historyBefore = data.historyCount(id);
        doThrow(new IllegalStateException("simulated history failure"))
                .when(historyRepository).save(any(DeliveryStatusHistory.class));

        assertThatThrownBy(() -> service.changeStatus(id, OUT_FOR_DELIVERY, null, null, principal(agent)))
                .hasMessageContaining("simulated history failure");

        assertThat(data.status(id)).isEqualTo("ASSIGNED");
        assertThat(data.historyCount(id)).isEqualTo(historyBefore);
    }

    @Test
    void editingChangesOnlyBusinessFieldsAndDeliveredRecordsAreLocked() {
        DeliveryView view = data.delivery(dispatcher, agent, "Old Name", "REF-1");
        DeliveryForm form = DeliveryForm.from(view);
        form.setRecipientName("New Name");
        form.setAssignedAgentId(otherAgent.getId());

        DeliveryView updated = service.update(view.id(), form, view.version(), principal(dispatcher));

        assertThat(updated.recipientName()).isEqualTo("New Name");
        assertThat(updated.status()).isEqualTo(ASSIGNED);
        assertThat(updated.assignedAgentName()).isEqualTo("Ari Agent");
        assertThat(updated.history()).hasSize(view.history().size());

        service.changeStatus(view.id(), OUT_FOR_DELIVERY, null, null, principal(agent));
        service.changeStatus(view.id(), DELIVERED, null, null, principal(agent));
        assertThatThrownBy(() -> service.update(view.id(), form, null, principal(dispatcher)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void aSessionWhoseAccountWasDeactivatedOrChangedRoleIsRefused() {
        long id = data.delivery(dispatcher, agent).id();
        var staleAgentSession = principal(agent);
        jdbc.update("UPDATE users SET active = false WHERE id = ?", agent.getId());
        assertThatThrownBy(() -> service.changeStatus(id, OUT_FOR_DELIVERY, null, null, staleAgentSession))
                .isInstanceOf(AccessDeniedException.class);

        var staleDispatcherSession = principal(dispatcher);
        jdbc.update("UPDATE users SET role = 'AGENT' WHERE id = ?", dispatcher.getId());
        assertThatThrownBy(() -> service.assign(id, otherAgent.getId(), null, staleDispatcherSession))
                .isInstanceOf(AccessDeniedException.class);
    }
}
