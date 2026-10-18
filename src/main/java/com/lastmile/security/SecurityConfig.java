package com.lastmile.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

/**
 * URL rules are a first line of defence only; services re-check role and ownership
 * (docs/Backend_Schema.md section 7).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] STAFF = {"ADMIN", "DISPATCHER"};

    /**
     * JSON API (decision D9, revised): stateless HTTP Basic only. Session cookies are ignored here,
     * which is what makes disabling CSRF for /api/** safe — a cross-site request cannot ride on a
     * browser session. Health endpoints are public with minimal payloads.
     */
    @Bean
    @Order(1)
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/health/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/deliveries").hasAnyRole(STAFF)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/deliveries/*").hasAnyRole(STAFF)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/deliveries/*/assignment").hasAnyRole(STAFF)
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityContext(context -> context.securityContextRepository(new RequestAttributeSecurityContextRepository()))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/webjars/**", "/favicon.ico", "/error").permitAll()
                        .requestMatchers("/deliveries/new", "/deliveries/*/edit", "/deliveries/*/assignment").hasAnyRole(STAFF)
                        .requestMatchers("/admin/**").hasRole("ADMIN")
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
