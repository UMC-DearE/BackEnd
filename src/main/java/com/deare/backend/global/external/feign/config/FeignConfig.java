package com.deare.backend.global.external.feign.config;

import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    /*
     * 추후에 ExternalApiException, ErrorCode 매핑
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    @Bean
    public Retryer feignRetryer() {
        // period, maxPeriod, maxAttempts(최초 시도 포함 총 시도 횟수)
        return new LoggingRetryer(1_000, 3_000, 3);
    }

}
