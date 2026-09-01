package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.letter.search.BlindIndexKeyProvider;
import com.deare.backend.domain.letter.search.BlindIndexKeyVersion;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LetterSearchTokenBackfillServiceTest {

    private final LetterRepository letterRepository = mock(LetterRepository.class);
    private final BlindIndexKeyProvider keyProvider = mock(BlindIndexKeyProvider.class);
    private final LetterSearchTokenBackfillProcessor processor =
            mock(LetterSearchTokenBackfillProcessor.class);
    private final LetterSearchTokenBackfillService service =
            new LetterSearchTokenBackfillService(letterRepository, keyProvider, processor);

    @Test
    void processesEachLetterIndependentlyAndReportsFailures() {
        when(keyProvider.currentVersion()).thenReturn(new BlindIndexKeyVersion(2));
        when(letterRepository.findActiveIdsMissingSearchTokenVersion(
                0,
                2,
                PageRequest.of(0, 3)
        )).thenReturn(List.of(10L, 20L, 30L));
        when(processor.indexIfMissing(10L, 2)).thenReturn(true);
        when(processor.indexIfMissing(20L, 2)).thenReturn(false);
        when(processor.indexIfMissing(30L, 2)).thenThrow(new IllegalStateException());

        assertThat(service.backfillNextBatch(0, 3))
                .isEqualTo(new LetterSearchTokenBackfillService.BackfillBatchResult(
                        3, 1, 1, 30L
                ));
    }

    @Test
    void clampsRequestedBatchSizeToSafeBoundary() {
        service.findNextTargetIds(-1, 0, 1);
        service.findNextTargetIds(20, 10_000, 1);

        verify(letterRepository).findActiveIdsMissingSearchTokenVersion(
                0,
                1,
                PageRequest.of(0, 1)
        );
        verify(letterRepository).findActiveIdsMissingSearchTokenVersion(
                20,
                1,
                PageRequest.of(0, 500)
        );
    }

    @Test
    void keepsCursorWhenNoTargetsRemain() {
        when(keyProvider.currentVersion()).thenReturn(new BlindIndexKeyVersion(2));
        when(letterRepository.findActiveIdsMissingSearchTokenVersion(
                30,
                2,
                PageRequest.of(0, 50)
        )).thenReturn(List.of());

        assertThat(service.backfillNextBatch(30, 50))
                .isEqualTo(new LetterSearchTokenBackfillService.BackfillBatchResult(
                        0, 0, 0, 30L
                ));
    }
}
