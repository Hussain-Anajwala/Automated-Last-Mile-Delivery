package com.lastmile.security;

import java.util.Collection;
import java.util.List;

import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Authenticated principal. The user id held here is the only source of actor identity for
 * audit fields; request parameters are never trusted for it.
 */
public final class AppUserDetails implements UserDetails {

    private final long id;
    private final String email;
    private final String fullName;
    private final String passwordHash;
    private final Role role;
    private final boolean active;

    private AppUserDetails(long id, String email, String fullName, String passwordHash, Role role, boolean active) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }

    public static AppUserDetails from(UserAccount user) {
        return new AppUserDetails(user.getId(), user.getEmail(), user.getFullName(),
                user.getPasswordHash(), user.getRole(), user.isActive());
    }

    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.authority()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
