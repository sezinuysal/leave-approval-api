package com.sezinsu.leaveapproval.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {

    /** Injected so that time can be controlled in tests. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
