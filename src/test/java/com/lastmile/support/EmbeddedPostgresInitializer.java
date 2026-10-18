package com.lastmile.support;

import java.io.IOException;
import java.io.UncheckedIOException;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Starts one real PostgreSQL 17 server per test JVM (decision D11) and points Spring at it.
 * The server listens on a random free localhost port with trust authentication, so no password
 * is involved.
 */
public final class EmbeddedPostgresInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static EmbeddedPostgres postgres;

    static synchronized EmbeddedPostgres postgres() {
        if (postgres == null) {
            try {
                postgres = EmbeddedPostgres.builder().start();
            } catch (IOException ex) {
                throw new UncheckedIOException("Could not start embedded PostgreSQL", ex);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    postgres.close();
                } catch (IOException ignored) {
                    // JVM is exiting
                }
            }));
        }
        return postgres;
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        TestPropertyValues.of(
                "spring.datasource.url=" + postgres().getJdbcUrl("postgres", "postgres"),
                "spring.datasource.username=postgres",
                "spring.datasource.password=")
                .applyTo(context);
    }
}
