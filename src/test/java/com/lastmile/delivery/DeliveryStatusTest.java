package com.lastmile.delivery;

import static com.lastmile.delivery.DeliveryStatus.ASSIGNED;
import static com.lastmile.delivery.DeliveryStatus.CREATED;
import static com.lastmile.delivery.DeliveryStatus.DELIVERED;
import static com.lastmile.delivery.DeliveryStatus.FAILED;
import static com.lastmile.delivery.DeliveryStatus.OUT_FOR_DELIVERY;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DeliveryStatusTest {

    /** The complete permitted state machine from docs/Backend_Schema.md section 6. */
    private static final Map<DeliveryStatus, Set<DeliveryStatus>> ALLOWED = Map.of(
            CREATED, Set.of(ASSIGNED),
            ASSIGNED, Set.of(OUT_FOR_DELIVERY),
            OUT_FOR_DELIVERY, Set.of(DELIVERED, FAILED),
            FAILED, Set.of(ASSIGNED),
            DELIVERED, Set.of());

    @ParameterizedTest
    @EnumSource(DeliveryStatus.class)
    void everyPairMatchesTheDocumentedStateMachine(DeliveryStatus from) {
        for (DeliveryStatus to : DeliveryStatus.values()) {
            assertThat(from.canTransitionTo(to))
                    .as("%s -> %s", from, to)
                    .isEqualTo(ALLOWED.get(from).contains(to));
        }
    }

    @Test
    void deliveredIsTheOnlyTerminalState() {
        assertThat(DELIVERED.isTerminal()).isTrue();
        assertThat(Set.of(CREATED, ASSIGNED, OUT_FOR_DELIVERY, FAILED))
                .allSatisfy(s -> assertThat(s.isTerminal()).isFalse());
    }
}
