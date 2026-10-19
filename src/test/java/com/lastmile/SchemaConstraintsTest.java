package com.lastmile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lastmile.support.IntegrationTest;
import com.lastmile.support.TestData;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

/** The database itself rejects invalid data, as a second line behind the services. */
@IntegrationTest
class SchemaConstraintsTest {

    @Autowired JdbcTemplate jdbc;
    @Autowired TestData data;

    UserAccount dispatcher;

    @BeforeEach
    void setUp() {
        data.reset();
        dispatcher = data.user("Dina Dispatcher", Role.DISPATCHER);
    }

    private void insertDelivery(String code, String status, String failureReason, Long agentId) {
        jdbc.update("""
                INSERT INTO deliveries (delivery_code, recipient_name, recipient_phone, delivery_address, delivery_city,
                    created_by_user_id, assigned_agent_id, status, failure_reason, created_at, updated_at)
                VALUES (?, 'R', '1234567', 'A', 'C', ?, ?, ?, ?, now(), now())
                """, code, dispatcher.getId(), agentId, status, failureReason);
    }

    @Test
    void flywayAppliedTheCoreMigration() {
        Integer applied = jdbc.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE version = '1' AND success", Integer.class);
        assertThat(applied).isEqualTo(1);
    }

    @Test
    void deliveryCodeIsUnique() {
        insertDelivery("LMD-1", "CREATED", null, null);
        assertThatThrownBy(() -> insertDelivery("LMD-1", "CREATED", null, null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void unknownStatusIsRejected() {
        assertThatThrownBy(() -> insertDelivery("LMD-2", "LOST", null, null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void failedRequiresAReason() {
        assertThatThrownBy(() -> insertDelivery("LMD-3", "FAILED", "  ", null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void assignedRequiresAnAgent() {
        assertThatThrownBy(() -> insertDelivery("LMD-4", "ASSIGNED", null, null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void emailMustBeUniqueAndNormalized() {
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO users (full_name, email, password_hash, role, created_at, updated_at)
                VALUES ('X', 'Mixed@Case.test', 'h', 'AGENT', now(), now())"""))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO users (full_name, email, password_hash, role, created_at, updated_at)
                VALUES ('X', ?, 'h', 'AGENT', now(), now())""", dispatcher.getEmail()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void historyCannotReferenceMissingDelivery() {
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO delivery_status_history (delivery_id, changed_by_user_id, new_status, changed_at)
                VALUES (424242, ?, 'CREATED', now())""", dispatcher.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
