package com.lastmile.user;

import java.time.Clock;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates fictional demo accounts, only when the {@code demo} profile is active AND
 * {@code LASTMILE_DEMO_PASSWORD} is set. There is no built-in default password, so demo
 * credentials can never become a deployment default (Backend_Schema section 10).
 * Existing accounts are left untouched, which keeps the seeder idempotent.
 */
@Component
@Profile("demo")
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    static final List<DemoUser> DEMO_USERS = List.of(
            new DemoUser("Asha Admin", "admin@lastmile.test", Role.ADMIN),
            new DemoUser("Dev Dispatcher", "dispatcher@lastmile.test", Role.DISPATCHER),
            new DemoUser("Arjun Agent", "agent.one@lastmile.test", Role.AGENT),
            new DemoUser("Bela Agent", "agent.two@lastmile.test", Role.AGENT));

    record DemoUser(String fullName, String email, Role role) {
    }

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final String demoPassword;

    public DemoDataSeeder(UserAccountRepository users, PasswordEncoder passwordEncoder, Clock clock,
                          @Value("${LASTMILE_DEMO_PASSWORD:}") String demoPassword) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (demoPassword == null || demoPassword.length() < 12) {
            log.warn("demo profile active but LASTMILE_DEMO_PASSWORD is missing or shorter than 12 characters; "
                    + "no demo accounts created");
            return;
        }
        String hash = passwordEncoder.encode(demoPassword);
        int created = 0;
        for (DemoUser demo : DEMO_USERS) {
            if (!users.existsByEmail(demo.email())) {
                users.save(new UserAccount(demo.fullName(), demo.email(), hash, demo.role(), clock.instant()));
                created++;
            }
        }
        log.info("Demo accounts ensured: {} created, {} already present", created, DEMO_USERS.size() - created);
    }
}
