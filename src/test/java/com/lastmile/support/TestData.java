package com.lastmile.support;

import java.time.Instant;

import com.lastmile.delivery.DeliveryForm;
import com.lastmile.delivery.DeliveryService;
import com.lastmile.delivery.DeliveryView;
import com.lastmile.security.AppUserDetails;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import com.lastmile.user.UserAccountRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Creates fictional users and resets tables so each test starts from a known state. */
public class TestData {

    /** Test-only password for fictional accounts in the embedded test database. */
    public static final String PASSWORD = "test-only-password";

    private final JdbcTemplate jdbc;
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final DeliveryService deliveries;

    public TestData(JdbcTemplate jdbc, UserAccountRepository users, PasswordEncoder passwordEncoder,
                    DeliveryService deliveries) {
        this.jdbc = jdbc;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.deliveries = deliveries;
    }

    /** Creates a delivery through the real service (so history and rules apply). */
    public DeliveryView delivery(UserAccount creator, UserAccount agent, String recipient, String reference) {
        DeliveryForm form = new DeliveryForm();
        form.setRecipientName(recipient);
        form.setRecipientPhone("9876543210");
        form.setDeliveryAddress("1 Test Street");
        form.setDeliveryCity("Pune");
        form.setReferenceNumber(reference);
        form.setAssignedAgentId(agent == null ? null : agent.getId());
        return deliveries.create(form, principal(creator));
    }

    public DeliveryView delivery(UserAccount creator, UserAccount agent) {
        return delivery(creator, agent, "Test Recipient", null);
    }

    public String status(long deliveryId) {
        return jdbc.queryForObject("SELECT status FROM deliveries WHERE id = ?", String.class, deliveryId);
    }

    public int historyCount(long deliveryId) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM delivery_status_history WHERE delivery_id = ?",
                Integer.class, deliveryId);
        return n == null ? 0 : n;
    }

    public void reset() {
        jdbc.execute("TRUNCATE delivery_status_history, deliveries, users RESTART IDENTITY CASCADE");
    }

    public UserAccount user(String name, Role role) {
        String email = name.toLowerCase().replace(' ', '.') + "@lastmile.test";
        return users.save(new UserAccount(name, email, passwordEncoder.encode(PASSWORD), role, Instant.now()));
    }

    public UserAccount inactiveUser(String name, Role role) {
        UserAccount user = user(name, role);
        user.deactivate(Instant.now());
        return users.save(user);
    }

    public static AppUserDetails principal(UserAccount user) {
        return AppUserDetails.from(user);
    }

    public int count(String table) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM " + table, Integer.class);
        return n == null ? 0 : n;
    }
}
