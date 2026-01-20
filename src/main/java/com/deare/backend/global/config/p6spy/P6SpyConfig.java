package com.deare.backend.global.config.p6spy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@Configuration
public class P6SpyConfig {

    @Bean
    public P6SpyEventListener p6SpyCustomEventListener() {
        return new P6SpyEventListener();
    }

    @Bean
    public P6spyPrettySqlFormatter p6SpyCustomFormatter() {
        return new P6spyPrettySqlFormatter();
    }
}



