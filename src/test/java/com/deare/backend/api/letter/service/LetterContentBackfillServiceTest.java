package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.repository.LetterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LetterContentBackfillServiceTest {

    private final LetterRepository letterRepository = mock(LetterRepository.class);
    private final LetterContentBackfillProcessor processor =
            mock(LetterContentBackfillProcessor.class);
    private final LetterContentBackfillService service =
            new LetterContentBackfillService(letterRepository, processor);

    @Test
    void processesEachLetterIndependentlyAndReportsFailures() {
        when(letterRepository.findIdsMissingEncryptedContent(
                0,
                PageRequest.of(0, 3)
        )).thenReturn(List.of(10L, 20L, 30L));
        when(processor.encryptIfMissing(10L)).thenReturn(true);
        when(processor.encryptIfMissing(20L)).thenReturn(false);
        when(processor.encryptIfMissing(30L)).thenThrow(new IllegalStateException());

        assertThat(service.backfillNextBatch(0, 3))
                .isEqualTo(new LetterContentBackfillService.BackfillBatchResult(
                        3, 1, 1, 30L
                ));
    }

    @Test
    void clampsRequestedBatchSizeToSafeBoundary() {
        service.findNextTargetIds(-1, 0);
        service.findNextTargetIds(20, 10_000);

        verify(letterRepository).findIdsMissingEncryptedContent(
                0,
                PageRequest.of(0, 1)
        );
        verify(letterRepository).findIdsMissingEncryptedContent(
                20,
                PageRequest.of(0, 500)
        );
    }

    @Test
    void keepsCursorWhenNoTargetsRemain() {
        when(letterRepository.findIdsMissingEncryptedContent(
                30,
                PageRequest.of(0, 50)
        )).thenReturn(List.of());

        assertThat(service.backfillNextBatch(30, 50))
                .isEqualTo(new LetterContentBackfillService.BackfillBatchResult(
                        0, 0, 0, 30L
                ));
    }
}
