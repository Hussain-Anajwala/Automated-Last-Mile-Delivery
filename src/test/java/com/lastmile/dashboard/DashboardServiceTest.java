package com.lastmile.dashboard;

import static com.lastmile.support.TestData.principal;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import com.lastmile.delivery.DeliveryService;
import com.lastmile.delivery.DeliveryStatus;
import com.lastmile.support.IntegrationTest;
import com.lastmile.support.TestData;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** Dashboard numbers must equal direct SQL counts for the caller's scope (acceptance A7). */
@IntegrationTest
class DashboardServiceTest {

    @Autowired DashboardService dashboard;
    @Autowired DeliveryService deliveries;
    @Autowired TestData data;
    @Autowired JdbcTemplate jdbc;

    UserAccount dispatcher;
    UserAccount agent;
    UserAccount otherAgent;

    @BeforeEach
    void setUp() {
        data.reset();
        dispatcher = data.user("Dina Dispatcher", Role.DISPATCHER);
        agent = data.user("Ari Agent", Role.AGENT);
        otherAgent = data.user("Oli Agent", Role.AGENT);

        data.delivery(dispatcher, null);
        data.delivery(dispatcher, null);
        long out = data.delivery(dispatcher, agent).id();
        deliveries.changeStatus(out, DeliveryStatus.OUT_FOR_DELIVERY, null, null, principal(agent));
        long failed = data.delivery(dispatcher, agent).id();
        deliveries.changeStatus(failed, DeliveryStatus.OUT_FOR_DELIVERY, null, null, principal(agent));
        deliveries.changeStatus(failed, DeliveryStatus.FAILED, "Closed", null, principal(agent));
        long delivered = data.delivery(dispatcher, otherAgent).id();
        deliveries.changeStatus(delivered, DeliveryStatus.OUT_FOR_DELIVERY, null, null, principal(otherAgent));
        deliveries.changeStatus(delivered, DeliveryStatus.DELIVERED, null, null, principal(otherAgent));
        data.delivery(dispatcher, otherAgent);
    }

    private Map<DeliveryStatus, Long> sqlCounts(Long agentId) {
        Map<DeliveryStatus, Long> counts = new HashMap<>();
        for (DeliveryStatus s : DeliveryStatus.values()) {
            counts.put(s, 0L);
        }
        String sql = "SELECT status, count(*) FROM deliveries" + (agentId == null ? "" : " WHERE assigned_agent_id = ?")
                + " GROUP BY status";
        Object[] args = agentId == null ? new Object[0] : new Object[] {agentId};
        jdbc.query(sql, rs -> {
            counts.put(DeliveryStatus.valueOf(rs.getString(1)), rs.getLong(2));
        }, args);
        return counts;
    }

    @Test
    void staffCountsMatchTheDatabaseForAllDeliveries() {
        DashboardSummary summary = dashboard.summary(principal(dispatcher));

        assertThat(summary.scope()).isEqualTo("ALL_DELIVERIES");
        assertThat(summary.total()).isEqualTo(6);
        assertThat(summary.countsByStatus()).isEqualTo(sqlCounts(null));
        assertThat(summary.count(DeliveryStatus.CREATED)).isEqualTo(2);
        assertThat(summary.recent()).hasSize(5);
    }

    @Test
    void agentCountsCoverOnlyTheirOwnAssignments() {
        DashboardSummary summary = dashboard.summary(principal(agent));

        assertThat(summary.scope()).isEqualTo("MY_ASSIGNMENTS");
        assertThat(summary.total()).isEqualTo(2);
        assertThat(summary.countsByStatus()).isEqualTo(sqlCounts(agent.getId()));
        assertThat(summary.count(DeliveryStatus.DELIVERED)).isZero();
        assertThat(summary.recent()).hasSize(2);
    }

    @Test
    void countsReflectAStatusChangeImmediately() {
        long id = data.delivery(dispatcher, agent).id();
        long before = dashboard.summary(principal(agent)).count(DeliveryStatus.OUT_FOR_DELIVERY);

        deliveries.changeStatus(id, DeliveryStatus.OUT_FOR_DELIVERY, null, null, principal(agent));

        assertThat(dashboard.summary(principal(agent)).count(DeliveryStatus.OUT_FOR_DELIVERY)).isEqualTo(before + 1);
    }
}
