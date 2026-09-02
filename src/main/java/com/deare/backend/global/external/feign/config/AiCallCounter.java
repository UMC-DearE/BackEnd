package com.deare.backend.global.external.feign.config;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class AiCallCounter {

    // OCR 실제 API 호출 횟수 (최초 시도 + 재시도 포함)
    private final AtomicLong ocrCount = new AtomicLong(0);

    // 편지분석 실제 API 호출 횟수 (최초 시도 + 재시도 포함)
    private final AtomicLong analyzeCount = new AtomicLong(0);

    // 리포트분석 실제 API 호출 횟수 (최초 시도 + 재시도 포함)
    private final AtomicLong reportCount = new AtomicLong(0);

    // 전체 AI API 호출 횟수 누적 (OCR + ANALYZE + REPORT, 재시도 포함)
    private final AtomicLong totalCount = new AtomicLong(0);

    public record CallCount(long attemptSeq, long total) {}

    public CallCount nextOcr() {
        long total = totalCount.incrementAndGet();
        long attemptSeq = ocrCount.incrementAndGet();
        return new CallCount(attemptSeq, total);
    }

    public CallCount nextAnalyze() {
        long total = totalCount.incrementAndGet();
        long attemptSeq = analyzeCount.incrementAndGet();
        return new CallCount(attemptSeq, total);
    }

    public CallCount nextReport() {
        long total = totalCount.incrementAndGet();
        long attemptSeq = reportCount.incrementAndGet();
        return new CallCount(attemptSeq, total);
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
