package com.lastmile.delivery;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Generates codes like {@code LMD-20261019-000042}. The numeric part comes from a PostgreSQL
 * sequence, so codes are unique without retry loops; the unique constraint is a second guard.
 */
@Component
public class DeliveryCodeGenerator {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    private final JdbcTemplate jdbcTemplate;

    public DeliveryCodeGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String next(Instant now) {
        Long sequence = jdbcTemplate.queryForObject("SELECT nextval('delivery_code_seq')", Long.class);
        return format(now, sequence);
    }

    static String format(Instant now, long sequence) {
        return "LMD-" + DATE.format(now) + "-" + String.format("%06d", sequence);
    }
}
