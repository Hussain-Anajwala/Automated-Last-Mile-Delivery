package com.lastmile.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    /** All server-generated timestamps use this UTC clock (replaceable in tests). */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
