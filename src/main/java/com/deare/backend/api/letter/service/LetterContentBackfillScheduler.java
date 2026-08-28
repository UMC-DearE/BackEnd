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
                "letter-content.backfill.enabled",
                "letter-content.encryption.enabled"
        },
        havingValue = "true"
)
public class LetterContentBackfillScheduler {

    private final LetterContentBackfillService backfillService;
    private long afterId;
    private boolean retryRequired;
    private boolean completed;
    private int retryPasses;

    @Value("${letter-content.backfill.batch-size:50}")
    private int batchSize;

    @Value("${letter-content.backfill.max-retry-passes:3}")
    private int maxRetryPasses;

    @Scheduled(
            fixedDelayString = "${letter-content.backfill.fixed-delay-ms:60000}",
            initialDelayString = "${letter-content.backfill.initial-delay-ms:30000}"
    )
    public void backfill() {
        if (completed) {
            return;
        }

        LetterContentBackfillService.BackfillBatchResult result =
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
                            "Letter content backfill stopped with unresolved failures. retryPasses={}",
                            retryPasses
                    );
                } else {
                    retryPasses++;
                    afterId = 0;
                    retryRequired = false;
                    log.warn(
                            "Letter content backfill pass completed with failures. retryPass={}/{}",
                            retryPasses,
                            retryLimit
                    );
                }
            } else {
                completed = true;
                log.info("Letter content backfill completed.");
            }
            return;
        }

        afterId = result.lastScannedId();
        log.info(
                "Letter content backfill batch completed. scanned={}, encrypted={}, failed={}",
                result.scanned(),
                result.encrypted(),
                result.failed()
        );
    }
}
