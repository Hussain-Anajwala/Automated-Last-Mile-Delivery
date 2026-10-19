package com.lastmile.delivery;

import java.util.List;

import com.lastmile.user.Role;

/**
 * Single source of truth for who may do what to a delivery in a given state. The service uses
 * these rules to authorize requests and the views use the same rules to decide which controls to
 * show, so a visible control is always one the server will accept. Ownership (an agent may act only
 * on deliveries assigned to them) is enforced separately when the delivery is loaded.
 */
public final class DeliveryPolicy {

    private DeliveryPolicy() {
    }

    public static boolean canEdit(Role role, DeliveryStatus status) {
        return role.isStaff() && status != DeliveryStatus.DELIVERED;
    }

    /** Assignment from CREATED/FAILED, or reassignment while still ASSIGNED (decision D7). */
    public static boolean canAssign(Role role, DeliveryStatus status) {
        return role.isStaff()
                && (status == DeliveryStatus.CREATED || status == DeliveryStatus.ASSIGNED || status == DeliveryStatus.FAILED);
    }

    /**
     * Status changes requested directly. {@code -> ASSIGNED} only happens through assignment, which
     * needs an agent. ADMIN and DISPATCHER follow the same state machine as agents (decision D6).
     */
    public static List<DeliveryStatus> statusTargets(Role role, DeliveryStatus status) {
        return status.allowedNext().stream()
                .filter(next -> next != DeliveryStatus.ASSIGNED)
                .toList();
    }

    public static boolean canChangeStatus(Role role, DeliveryStatus from, DeliveryStatus to) {
        return statusTargets(role, from).contains(to);
    }

    public record Actions(boolean canEdit, boolean canAssign, List<DeliveryStatus> statusTargets) {
    }

    public static Actions actionsFor(Role role, DeliveryStatus status) {
        return new Actions(canEdit(role, status), canAssign(role, status), statusTargets(role, status));
    }
}
