package com.deare.backend.api.letter.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LetterContentBackfillSchedulerTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(LetterContentBackfillScheduler.class);

    @Test
    void createsSchedulerWhenBackfillAndEncryptionAreEnabled() {
        contextRunner
                .withBean(
                        LetterContentBackfillService.class,
                        () -> mock(LetterContentBackfillService.class)
                )
                .withPropertyValues(
                        "letter-content.backfill.enabled=true",
                        "letter-content.encryption.enabled=true"
                )
                .run(context -> assertThat(context)
                        .hasSingleBean(LetterContentBackfillScheduler.class));
    }

    @Test
    void doesNotCreateSchedulerWhenBackfillIsEnabledWithoutEncryption() {
        contextRunner
                .withPropertyValues(
                        "letter-content.backfill.enabled=true",
                        "letter-content.encryption.enabled=false"
                )
                .run(context -> assertThat(context)
                        .hasNotFailed()
                        .doesNotHaveBean(LetterContentBackfillScheduler.class));
    }

    @Test
    void stopsQueryingAfterSuccessfulPassCompletion() {
        LetterContentBackfillService service = mock(LetterContentBackfillService.class);
        LetterContentBackfillScheduler scheduler = scheduler(service);
        when(service.backfillNextBatch(0, 50)).thenReturn(batch(0, 0, 0, 0));

        scheduler.backfill();
        scheduler.backfill();

        verify(service, times(1)).backfillNextBatch(0, 50);
    }

    @Test
    void stopsAfterRetryPassLimitWhenFailurePersists() {
        LetterContentBackfillService service = mock(LetterContentBackfillService.class);
        LetterContentBackfillScheduler scheduler = scheduler(service);
        ReflectionTestUtils.setField(scheduler, "maxRetryPasses", 1);
        when(service.backfillNextBatch(anyLong(), eq(50))).thenReturn(
                batch(1, 0, 1, 10),
                batch(0, 0, 0, 10),
                batch(1, 0, 1, 10),
                batch(0, 0, 0, 10)
        );

        for (int attempt = 0; attempt < 5; attempt++) {
            scheduler.backfill();
        }

        verify(service, times(4)).backfillNextBatch(anyLong(), eq(50));
    }

    private LetterContentBackfillScheduler scheduler(
            LetterContentBackfillService service
    ) {
        LetterContentBackfillScheduler scheduler =
                new LetterContentBackfillScheduler(service);
        ReflectionTestUtils.setField(scheduler, "batchSize", 50);
        ReflectionTestUtils.setField(scheduler, "maxRetryPasses", 3);
        return scheduler;
    }

    private LetterContentBackfillService.BackfillBatchResult batch(
            int scanned,
            int encrypted,
            int failed,
            long lastScannedId
    ) {
        return new LetterContentBackfillService.BackfillBatchResult(
                scanned,
                encrypted,
                failed,
                lastScannedId
        );
    }
}
