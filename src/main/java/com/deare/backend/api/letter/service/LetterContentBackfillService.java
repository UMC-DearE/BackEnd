package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.repository.LetterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "letter-content.encryption",
        name = "enabled",
        havingValue = "true"
)
public class LetterContentBackfillService {

    private static final int MAX_BATCH_SIZE = 500;

    private final LetterRepository letterRepository;
    private final LetterContentBackfillProcessor processor;

    public List<Long> findNextTargetIds(long afterId, int requestedBatchSize) {
        int batchSize = Math.max(1, Math.min(requestedBatchSize, MAX_BATCH_SIZE));
        return letterRepository.findIdsMissingEncryptedContent(
                Math.max(0, afterId),
                PageRequest.of(0, batchSize)
        );
    }

    public BackfillBatchResult backfillNextBatch(long afterId, int requestedBatchSize) {
        List<Long> targetIds = findNextTargetIds(afterId, requestedBatchSize);
        int encrypted = 0;
        int failed = 0;

        for (Long targetId : targetIds) {
            try {
                if (processor.encryptIfMissing(targetId)) {
                    encrypted++;
                }
            } catch (RuntimeException exception) {
                failed++;
                log.error(
                        "Letter content backfill failed. letterId={}, errorType={}",
                        targetId,
                        exception.getClass().getSimpleName()
                );
            }
        }

        long lastScannedId = targetIds.isEmpty()
                ? afterId
                : targetIds.get(targetIds.size() - 1);
        return new BackfillBatchResult(targetIds.size(), encrypted, failed, lastScannedId);
    }

    public record BackfillBatchResult(
            int scanned,
            int encrypted,
            int failed,
            long lastScannedId
    ) {
    }
}
