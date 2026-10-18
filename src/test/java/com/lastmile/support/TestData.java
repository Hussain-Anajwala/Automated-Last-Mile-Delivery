package com.lastmile.support;

import java.time.Instant;

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

    public TestData(JdbcTemplate jdbc, UserAccountRepository users, PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
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
