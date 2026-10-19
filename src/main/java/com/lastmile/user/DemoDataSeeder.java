package com.lastmile.user;

import java.time.Clock;
import java.util.List;

import com.lastmile.delivery.DeliveryForm;
import com.lastmile.delivery.DeliveryRepository;
import com.lastmile.delivery.DeliveryService;
import com.lastmile.delivery.DeliveryStatus;
import com.lastmile.security.AppUserDetails;
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
 * Existing accounts are left untouched, and sample deliveries are only added to an empty table,
 * which keeps the seeder idempotent. Sample deliveries go through DeliveryService, so their status
 * history is produced by the real workflow rules.
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
    private final DeliveryRepository deliveryRepository;
    private final DeliveryService deliveryService;

    public DemoDataSeeder(UserAccountRepository users, PasswordEncoder passwordEncoder, Clock clock,
                          @Value("${LASTMILE_DEMO_PASSWORD:}") String demoPassword,
                          DeliveryRepository deliveryRepository, DeliveryService deliveryService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.demoPassword = demoPassword;
        this.deliveryRepository = deliveryRepository;
        this.deliveryService = deliveryService;
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
        if (deliveryRepository.count() == 0) {
            seedDeliveries();
        }
    }

    private void seedDeliveries() {
        AppUserDetails dispatcher = principal("dispatcher@lastmile.test");
        AppUserDetails agentOne = principal("agent.one@lastmile.test");
        AppUserDetails agentTwo = principal("agent.two@lastmile.test");

        create(dispatcher, "Kavya Rao", "Pune", "ORD-2001", null);
        create(dispatcher, "Imran Shaikh", "Mumbai", "ORD-2002", null);
        create(dispatcher, "Leela Nair", "Pune", "ORD-2003", agentOne);
        long out = create(dispatcher, "Farhan Ali", "Mumbai", "ORD-2004", agentOne);
        deliveryService.changeStatus(out, DeliveryStatus.OUT_FOR_DELIVERY, null, null, agentOne);
        long delivered = create(dispatcher, "Nisha Verma", "Thane", "ORD-2005", agentTwo);
        deliveryService.changeStatus(delivered, DeliveryStatus.OUT_FOR_DELIVERY, null, null, agentTwo);
        deliveryService.changeStatus(delivered, DeliveryStatus.DELIVERED, null, null, agentTwo);
        long failed = create(dispatcher, "Joel D'Souza", "Thane", "ORD-2006", agentTwo);
        deliveryService.changeStatus(failed, DeliveryStatus.OUT_FOR_DELIVERY, null, null, agentTwo);
        deliveryService.changeStatus(failed, DeliveryStatus.FAILED, "Recipient unavailable", null, agentTwo);
        log.info("Demo deliveries created: 6 (fictional)");
    }

    private AppUserDetails principal(String email) {
        return AppUserDetails.from(users.findByEmail(email).orElseThrow());
    }

    private long create(AppUserDetails creator, String recipient, String city, String reference, AppUserDetails agent) {
        DeliveryForm form = new DeliveryForm();
        form.setRecipientName(recipient);
        form.setRecipientPhone("9000000000");
        form.setDeliveryAddress("Sample address, " + city);
        form.setDeliveryCity(city);
        form.setReferenceNumber(reference);
        form.setAssignedAgentId(agent == null ? null : agent.getId());
        return deliveryService.create(form, creator).id();
    }
}
