package com.lastmile.user;

public enum Role {
    ADMIN,
    DISPATCHER,
    AGENT;

    /** Spring Security authority name, e.g. {@code ROLE_DISPATCHER}. */
    public String authority() {
        return "ROLE_" + name();
    }

    public boolean isStaff() {
        return this == ADMIN || this == DISPATCHER;
    }
}
