package com.lastmile.delivery;

import java.util.EnumSet;
import java.util.Set;

/** Delivery states and the permitted state machine (docs/Backend_Schema.md section 6). */
public enum DeliveryStatus {
    CREATED("Created"),
    ASSIGNED("Assigned"),
    OUT_FOR_DELIVERY("Out for delivery"),
    DELIVERED("Delivered"),
    FAILED("Failed");

    private final String label;

    DeliveryStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public Set<DeliveryStatus> allowedNext() {
        return switch (this) {
            case CREATED -> EnumSet.of(ASSIGNED);
            case ASSIGNED -> EnumSet.of(OUT_FOR_DELIVERY);
            case OUT_FOR_DELIVERY -> EnumSet.of(DELIVERED, FAILED);
            case FAILED -> EnumSet.of(ASSIGNED);
            case DELIVERED -> EnumSet.noneOf(DeliveryStatus.class);
        };
    }

    public boolean canTransitionTo(DeliveryStatus next) {
        return allowedNext().contains(next);
    }

    public boolean isTerminal() {
        return allowedNext().isEmpty();
    }
}
