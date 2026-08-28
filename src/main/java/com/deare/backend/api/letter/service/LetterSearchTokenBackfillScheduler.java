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
        name = {
                "blind-index.backfill.enabled",
                "blind-index.keys.enabled"
        },
        havingValue = "true"
)
public class LetterSearchTokenBackfillScheduler {

    private final LetterSearchTokenBackfillService backfillService;
    private long afterId;
    private boolean retryRequired;
    private boolean completed;
    private int retryPasses;

    @Value("${blind-index.backfill.batch-size:50}")
    private int batchSize;

    @Value("${blind-index.backfill.max-retry-passes:3}")
    private int maxRetryPasses;

    @Scheduled(
            fixedDelayString = "${blind-index.backfill.fixed-delay-ms:60000}",
            initialDelayString = "${blind-index.backfill.initial-delay-ms:30000}"
    )
    public void backfill() {
        if (completed) {
            return;
        }

        LetterSearchTokenBackfillService.BackfillBatchResult result =
                backfillService.backfillNextBatch(afterId, batchSize);
        if (result.failed() > 0) {
            retryRequired = true;
        }
        if (result.scanned() == 0) {
            if (retryRequired) {
                int retryLimit = Math.max(0, maxRetryPasses);
                if (retryPasses >= retryLimit) {
                    completed = true;
                    log.error(
                            "Blind index token backfill stopped with unresolved failures. retryPasses={}",
                            retryPasses
                    );
                } else {
                    retryPasses++;
                    afterId = 0;
                    retryRequired = false;
                    log.warn(
                            "Blind index token backfill pass completed with failures. retryPass={}/{}",
                            retryPasses,
                            retryLimit
                    );
                }
            } else {
                completed = true;
                log.info("Blind index token backfill completed.");
            }
            return;
        }

        afterId = result.lastScannedId();
        log.info(
                "Blind index token backfill batch completed. scanned={}, indexed={}, failed={}",
                result.scanned(),
                result.indexed(),
                result.failed()
        );
    }
}
