package com.lastmile.security;

import com.lastmile.user.UserAccount;
import com.lastmile.user.UserAccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserAccountRepository users;

    public AppUserDetailsService(UserAccountRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        // Inactive accounts load with enabled=false, so Spring Security rejects them with the
        // same generic message as a wrong password.
        return users.findByEmail(UserAccount.normalizeEmail(username))
                .map(AppUserDetails::from)
                .orElseThrow(() -> new UsernameNotFoundException("Bad credentials"));
    }
}
