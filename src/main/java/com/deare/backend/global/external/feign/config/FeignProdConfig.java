package com.deare.backend.global.external.feign.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!dev")
public class FeignProdConfig {
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public feign.Request.Options feignRequestOptions() {
        return new feign.Request.Options(10_000, 180_000);
    }
}
