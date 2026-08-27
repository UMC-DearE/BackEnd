package com.deare.backend.api.letter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "blind-index.backfill",
        name = "enabled",
        havingValue = "true"
)
public class LetterSearchTokenBackfillScheduler {

    private final LetterSearchTokenBackfillService backfillService;
    private long afterId;

    @Value("${blind-index.backfill.batch-size:50}")
    private int batchSize;

    @Scheduled(
            fixedDelayString = "${blind-index.backfill.fixed-delay-ms:60000}",
            initialDelayString = "${blind-index.backfill.initial-delay-ms:30000}"
    )
    public void backfill() {
        LetterSearchTokenBackfillService.BackfillBatchResult result =
                backfillService.backfillNextBatch(afterId, batchSize);
        afterId = result.lastScannedId();
        if (result.scanned() > 0) {
            log.info(
                    "Blind index token backfill batch completed. scanned={}, indexed={}, failed={}",
                    result.scanned(),
                    result.indexed(),
                    result.failed()
            );
        }
    }
}
