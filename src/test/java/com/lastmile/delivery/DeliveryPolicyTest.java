package com.lastmile.delivery;

import static com.lastmile.delivery.DeliveryStatus.ASSIGNED;
import static com.lastmile.delivery.DeliveryStatus.CREATED;
import static com.lastmile.delivery.DeliveryStatus.DELIVERED;
import static com.lastmile.delivery.DeliveryStatus.FAILED;
import static com.lastmile.delivery.DeliveryStatus.OUT_FOR_DELIVERY;
import static org.assertj.core.api.Assertions.assertThat;

import com.lastmile.user.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DeliveryPolicyTest {

    @ParameterizedTest
    @EnumSource(DeliveryStatus.class)
    void agentsCanNeverEditOrAssign(DeliveryStatus status) {
        assertThat(DeliveryPolicy.canEdit(Role.AGENT, status)).isFalse();
        assertThat(DeliveryPolicy.canAssign(Role.AGENT, status)).isFalse();
    }

    @Test
    void staffEditAnythingExceptDeliveredAndAssignOnlyBeforeDispatch() {
        for (Role staff : new Role[] {Role.ADMIN, Role.DISPATCHER}) {
            assertThat(DeliveryPolicy.canEdit(staff, DELIVERED)).isFalse();
            assertThat(DeliveryPolicy.canEdit(staff, OUT_FOR_DELIVERY)).isTrue();
            assertThat(DeliveryPolicy.canAssign(staff, CREATED)).isTrue();
            assertThat(DeliveryPolicy.canAssign(staff, ASSIGNED)).isTrue();
            assertThat(DeliveryPolicy.canAssign(staff, FAILED)).isTrue();
            assertThat(DeliveryPolicy.canAssign(staff, OUT_FOR_DELIVERY)).isFalse();
            assertThat(DeliveryPolicy.canAssign(staff, DELIVERED)).isFalse();
        }
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void directStatusTargetsFollowTheStateMachineButNeverAssigned(Role role) {
        assertThat(DeliveryPolicy.statusTargets(role, CREATED)).isEmpty();
        assertThat(DeliveryPolicy.statusTargets(role, ASSIGNED)).containsExactly(OUT_FOR_DELIVERY);
        assertThat(DeliveryPolicy.statusTargets(role, OUT_FOR_DELIVERY)).containsExactlyInAnyOrder(DELIVERED, FAILED);
        assertThat(DeliveryPolicy.statusTargets(role, FAILED)).isEmpty();
        assertThat(DeliveryPolicy.statusTargets(role, DELIVERED)).isEmpty();
    }
}
