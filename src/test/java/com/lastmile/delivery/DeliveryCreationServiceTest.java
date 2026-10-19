package com.lastmile.delivery;

import static com.lastmile.support.TestData.principal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.Set;

import com.lastmile.support.IntegrationTest;
import com.lastmile.support.TestData;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import com.lastmile.web.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;

@IntegrationTest
class DeliveryCreationServiceTest {

    @Autowired DeliveryService service;
    @Autowired TestData data;
    @Autowired JdbcTemplate jdbc;

    UserAccount dispatcher;
    UserAccount agent;

    @BeforeEach
    void setUp() {
        data.reset();
        dispatcher = data.user("Dina Dispatcher", Role.DISPATCHER);
        agent = data.user("Ari Agent", Role.AGENT);
    }

    static DeliveryForm validForm() {
        DeliveryForm form = new DeliveryForm();
        form.setRecipientName("  Aarav Sharma ");
        form.setRecipientPhone("9876543210");
        form.setDeliveryAddress("Example Street, Apartment 12");
        form.setDeliveryCity("Mumbai");
        form.setReferenceNumber("ORD-1001");
        return form;
    }

    @Test
    void createsDeliveryInCreatedStateWithGeneratedCodeCreatorAndInitialHistory() {
        DeliveryView view = service.create(validForm(), principal(dispatcher));

        assertThat(view.deliveryCode()).matches("LMD-\\d{8}-\\d{6}");
        assertThat(view.status()).isEqualTo(DeliveryStatus.CREATED);
        assertThat(view.recipientName()).isEqualTo("Aarav Sharma");
        assertThat(view.createdByName()).isEqualTo("Dina Dispatcher");
        assertThat(view.assignedAgentId()).isNull();
        assertThat(view.history()).singleElement().satisfies(h -> {
            assertThat(h.previousStatus()).isNull();
            assertThat(h.newStatus()).isEqualTo(DeliveryStatus.CREATED);
            assertThat(h.changedByName()).isEqualTo("Dina Dispatcher");
        });
        Long creatorId = jdbc.queryForObject("SELECT created_by_user_id FROM deliveries WHERE id = ?", Long.class, view.id());
        assertThat(creatorId).isEqualTo(dispatcher.getId());
    }

    @Test
    void creatingWithAnActiveAgentAssignsItAndRecordsBothTransitions() {
        DeliveryForm form = validForm();
        form.setAssignedAgentId(agent.getId());

        DeliveryView view = service.create(form, principal(dispatcher));

        assertThat(view.status()).isEqualTo(DeliveryStatus.ASSIGNED);
        assertThat(view.assignedAgentName()).isEqualTo("Ari Agent");
        assertThat(view.history()).extracting(DeliveryView.HistoryEntry::newStatus)
                .containsExactly(DeliveryStatus.CREATED, DeliveryStatus.ASSIGNED);
    }

    @Test
    void rejectsAssignmentToNonAgentOrInactiveAgentAndSavesNothing() {
        UserAccount inactiveAgent = data.inactiveUser("Old Agent", Role.AGENT);
        for (long badId : new long[] {dispatcher.getId(), inactiveAgent.getId(), 999_999L}) {
            DeliveryForm form = validForm();
            form.setAssignedAgentId(badId);

            assertThatThrownBy(() -> service.create(form, principal(dispatcher)))
                    .isInstanceOf(BusinessRuleException.class)
                    .extracting("field").isEqualTo("assignedAgentId");
        }
        assertThat(data.count("deliveries")).isZero();
        assertThat(data.count("delivery_status_history")).isZero();
    }

    @Test
    void agentsCannotCreateDeliveries() {
        assertThatThrownBy(() -> service.create(validForm(), principal(agent)))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(data.count("deliveries")).isZero();
    }

    @Test
    void deactivatedStaffCannotCreateEvenWithAnExistingSession() {
        UserAccount formerDispatcher = data.inactiveUser("Former Dispatcher", Role.DISPATCHER);

        assertThatThrownBy(() -> service.create(validForm(), principal(formerDispatcher)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void generatedCodesAreUnique() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 25; i++) {
            codes.add(service.create(validForm(), principal(dispatcher)).deliveryCode());
        }
        assertThat(codes).hasSize(25);
    }
}
