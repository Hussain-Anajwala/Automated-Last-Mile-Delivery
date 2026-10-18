package com.lastmile.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * URL rules are a first line of defence only; services re-check role and ownership
 * (docs/Backend_Schema.md section 7).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/webjars/**", "/favicon.ico", "/error").permitAll()
                        .requestMatchers("/api/v1/health/**").permitAll()
                        .requestMatchers("/deliveries/new").hasAnyRole("ADMIN", "DISPATCHER")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        // Return to the originally requested page when there was one; otherwise home.
                        .defaultSuccessUrl("/")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives(
                        "default-src 'self'; img-src 'self' data:; object-src 'none'; frame-ancestors 'none'")));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // Delegating encoder: BCrypt by default, prefix-tagged so the algorithm can evolve.
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
