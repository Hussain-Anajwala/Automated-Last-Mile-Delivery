package com.lastmile.delivery;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class DeliveryCodeGeneratorTest {

    @Test
    void formatsUtcDateAndZeroPaddedSequence() {
        assertThat(DeliveryCodeGenerator.format(Instant.parse("2026-10-19T23:30:00Z"), 42))
                .isEqualTo("LMD-20261019-000042");
    }

    @Test
    void usesUtcNotLocalDate() {
        // 00:30 UTC on 20 Oct is still 20 Oct, whatever the server time zone is.
        assertThat(DeliveryCodeGenerator.format(Instant.parse("2026-10-20T00:30:00Z"), 1))
                .isEqualTo("LMD-20261020-000001");
    }
}
