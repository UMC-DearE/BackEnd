package com.deare.backend.global.external.feign.config;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingRetryer implements Retryer {

    private final long period;
    private final long maxPeriod;
    private final int maxAttempts;
    private final Retryer delegate;
    private int attempt = 1;

    public LoggingRetryer(long period, long maxPeriod, int maxAttempts) {
        this.period = period;
        this.maxPeriod = maxPeriod;
        this.maxAttempts = maxAttempts;
        this.delegate = new Retryer.Default(period, maxPeriod, maxAttempts);
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        if (attempt < maxAttempts) {
            log.warn(
                    "[Feign Retry] {}/{}번째 시도 실패, 재시도 진행 - status={}, reason={}, url={}",
                    attempt, maxAttempts, e.status(), e.getMessage(), e.request().url()
            );
        } else {
            log.warn(
                    "[Feign Retry] {}/{}번째 시도까지 모두 실패, 재시도 소진 - status={}, reason={}, url={}",
                    attempt, maxAttempts, e.status(), e.getMessage(), e.request().url()
            );
        }

        attempt++;
        delegate.continueOrPropagate(e);
    }

    @Override
    public Retryer clone() {
        return new LoggingRetryer(period, maxPeriod, maxAttempts);
    }
}
