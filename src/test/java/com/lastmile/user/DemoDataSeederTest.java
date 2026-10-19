package com.lastmile.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;

import com.lastmile.delivery.DeliveryRepository;
import com.lastmile.delivery.DeliveryService;
import com.lastmile.support.IntegrationTest;
import com.lastmile.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@IntegrationTest
class DemoDataSeederTest {

    @Autowired UserAccountRepository users;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired Clock clock;
    @Autowired DeliveryRepository deliveries;
    @Autowired DeliveryService deliveryService;
    @Autowired TestData data;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        data.reset();
    }

    private DemoDataSeeder seeder(String password) {
        return new DemoDataSeeder(users, passwordEncoder, clock, password, deliveries, deliveryService);
    }

    @Test
    void withoutAStrongEnoughPasswordNothingIsCreated() {
        seeder("").run(new DefaultApplicationArguments());
        seeder("short").run(new DefaultApplicationArguments());
        assertThat(data.count("users")).isZero();
        assertThat(data.count("deliveries")).isZero();
    }

    @Test
    void seedsFictionalAccountsAndWorkflowConsistentDeliveriesOnceOnly() {
        seeder("local-demo-password-1").run(new DefaultApplicationArguments());
        seeder("local-demo-password-1").run(new DefaultApplicationArguments());

        assertThat(data.count("users")).isEqualTo(4);
        assertThat(data.count("deliveries")).isEqualTo(6);
        assertThat(jdbc.queryForList("SELECT DISTINCT status FROM deliveries", String.class))
                .containsExactlyInAnyOrder("CREATED", "ASSIGNED", "OUT_FOR_DELIVERY", "DELIVERED", "FAILED");
        Integer historyRows = jdbc.queryForObject("SELECT count(*) FROM delivery_status_history", Integer.class);
        assertThat(historyRows).isEqualTo(6 + 4 + 1 + 2 + 2);
        assertThat(jdbc.queryForList("SELECT email FROM users", String.class))
                .allSatisfy(email -> assertThat(email).endsWith("@lastmile.test"));
    }
}
