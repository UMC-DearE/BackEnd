package com.deare.backend.global.external.feign.config;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class LoggingRetryer implements Retryer {

    private final long period;
    private final long maxPeriod;
    private final int maxAttempts;
    private final Retryer delegate;
    private final AiCallCounter counter;
    private int attempt = 1;

    public LoggingRetryer(long period, long maxPeriod, int maxAttempts, AiCallCounter counter) {
        this.period = period;
        this.maxPeriod = maxPeriod;
        this.maxAttempts = maxAttempts;
        this.delegate = new Retryer.Default(period, maxPeriod, maxAttempts);
        this.counter = counter;
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        String callType = MDC.get("aiCallType") != null ? MDC.get("aiCallType") : "UNKNOWN";
        AiCallCounter.CallCount count = counter.nextByType(callType);
        MDC.put("aiSeq", String.valueOf(count.seq()));
        MDC.put("aiTotal", String.valueOf(count.total()));

        if (attempt < maxAttempts) {
            log.warn(
                    "{} callType={} seq={} total={} {}/{}번째 시도 실패, 재시도 진행 - status={}, reason={}",
                    AiCallLogTag.RETRY, callType, count.seq(), count.total(), attempt, maxAttempts, e.status(), e.getMessage()
            );
        } else {
            log.warn(
                    "{} callType={} seq={} total={} {}/{}번째 시도까지 모두 실패, 재시도 소진 - status={}, reason={}",
                    AiCallLogTag.RETRY, callType, count.seq(), count.total(), attempt, maxAttempts, e.status(), e.getMessage()
            );
        }

        attempt++;
        delegate.continueOrPropagate(e);
    }

    @Override
    public Retryer clone() {
        return new LoggingRetryer(period, maxPeriod, maxAttempts, counter);
    }
}
