package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.letter.search.BlindIndexKeyProvider;
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
        prefix = "blind-index.keys",
        name = "enabled",
        havingValue = "true"
)
public class LetterSearchTokenBackfillService {

    private static final int MAX_BATCH_SIZE = 500;

    private final LetterRepository letterRepository;
    private final BlindIndexKeyProvider keyProvider;
    private final LetterSearchTokenBackfillProcessor processor;

    public List<Long> findNextTargetIds(long afterId, int requestedBatchSize, int keyVersion) {
        int batchSize = Math.max(1, Math.min(requestedBatchSize, MAX_BATCH_SIZE));
        return letterRepository.findActiveIdsMissingSearchTokenVersion(
                Math.max(0, afterId),
                keyVersion,
                PageRequest.of(0, batchSize)
        );
    }

    public BackfillBatchResult backfillNextBatch(long afterId, int requestedBatchSize) {
        int keyVersion = keyProvider.currentVersion().value();
        List<Long> targetIds = findNextTargetIds(afterId, requestedBatchSize, keyVersion);
        int indexed = 0;
        int failed = 0;

        for (Long targetId : targetIds) {
            try {
                if (processor.indexIfMissing(targetId, keyVersion)) {
                    indexed++;
                }
            } catch (RuntimeException exception) {
                failed++;
                log.error(
                        "Blind index token backfill failed. letterId={}, errorType={}",
                        targetId,
                        exception.getClass().getSimpleName()
                );
            }
        }

        long lastScannedId = targetIds.isEmpty()
                ? afterId
                : targetIds.get(targetIds.size() - 1);
        return new BackfillBatchResult(targetIds.size(), indexed, failed, lastScannedId);
    }

    public record BackfillBatchResult(
            int scanned,
            int indexed,
            int failed,
            long lastScannedId
    ) {
    }
}
