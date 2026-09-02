package com.deare.backend.global.external.feign.config;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class AiCallCounter {

    private final AtomicLong ocrCount = new AtomicLong(0);
    private final AtomicLong analyzeCount = new AtomicLong(0);
    private final AtomicLong reportCount = new AtomicLong(0);
    private final AtomicLong totalCount = new AtomicLong(0);

    public record CallCount(long seq, long total) {}

    public CallCount nextOcr() {
        long total = totalCount.incrementAndGet();
        long seq = ocrCount.incrementAndGet();
        return new CallCount(seq, total);
    }

    public CallCount nextAnalyze() {
        long total = totalCount.incrementAndGet();
        long seq = analyzeCount.incrementAndGet();
        return new CallCount(seq, total);
    }

    public CallCount nextReport() {
        long total = totalCount.incrementAndGet();
        long seq = reportCount.incrementAndGet();
        return new CallCount(seq, total);
    }

    public CallCount nextByType(String callType) {
        return switch (callType) {
            case "OCR" -> nextOcr();
            case "ANALYZE" -> nextAnalyze();
            case "REPORT" -> nextReport();
            default -> new CallCount(totalCount.incrementAndGet(), totalCount.get());
        };
    }
}
