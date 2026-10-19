package com.lastmile;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.SpringApplication;

/**
 * Local development launcher that needs no Docker or installed database: starts a throwaway
 * embedded PostgreSQL 17 (data is discarded on exit) and runs the real application against it.
 *
 * <pre>
 *   $env:LASTMILE_DEMO_PASSWORD = "&lt;at least 12 chars, local only&gt;"
 *   .\mvnw.cmd spring-boot:test-run "-Dspring-boot.run.main-class=com.lastmile.DevApplication"
 * </pre>
 *
 * The demo profile seeds fictional accounts only when LASTMILE_DEMO_PASSWORD is set.
 */
public final class DevApplication {

    private DevApplication() {
    }

    public static void main(String[] args) throws Exception {
        EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                postgres.close();
            } catch (Exception ignored) {
                // exiting
            }
        }));
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl("postgres", "postgres"));
        System.setProperty("spring.datasource.username", "postgres");
        System.setProperty("spring.datasource.password", "");
        SpringApplication app = new SpringApplication(LastMileApplication.class);
        app.setAdditionalProfiles("demo");
        app.run(args);
    }
}
