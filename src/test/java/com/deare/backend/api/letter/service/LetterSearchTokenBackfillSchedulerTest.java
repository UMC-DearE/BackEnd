package com.deare.backend.api.letter.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LetterSearchTokenBackfillSchedulerTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(LetterSearchTokenBackfillScheduler.class);

    @Test
    void createsSchedulerWhenBackfillAndKeysAreEnabled() {
        contextRunner
                .withBean(
                        LetterSearchTokenBackfillService.class,
                        () -> mock(LetterSearchTokenBackfillService.class)
                )
                .withPropertyValues(
                        "blind-index.backfill.enabled=true",
                        "blind-index.keys.enabled=true"
                )
                .run(context -> assertThat(context)
                        .hasSingleBean(LetterSearchTokenBackfillScheduler.class));
    }

    @Test
    void doesNotCreateSchedulerWhenBackfillIsEnabledWithoutKeys() {
        contextRunner
                .withPropertyValues(
                        "blind-index.backfill.enabled=true",
                        "blind-index.keys.enabled=false"
                )
                .run(context -> assertThat(context)
                        .hasNotFailed()
                        .doesNotHaveBean(LetterSearchTokenBackfillScheduler.class));
    }

    @Test
    void doesNotCreateSchedulerWhenKeysAreEnabledWithoutBackfill() {
        contextRunner
                .withPropertyValues(
                        "blind-index.backfill.enabled=false",
                        "blind-index.keys.enabled=true"
                )
                .run(context -> assertThat(context)
                        .hasNotFailed()
                        .doesNotHaveBean(LetterSearchTokenBackfillScheduler.class));
    }

    @Test
    void stopsQueryingAfterSuccessfulPassCompletion() {
        LetterSearchTokenBackfillService service = mock(LetterSearchTokenBackfillService.class);
        LetterSearchTokenBackfillScheduler scheduler = scheduler(service);
        when(service.backfillNextBatch(0, 50)).thenReturn(batch(0, 0, 0, 0));

        scheduler.backfill();
        scheduler.backfill();

        verify(service, times(1)).backfillNextBatch(0, 50);
    }

    @Test
    void restartsFromBeginningAfterPassWithFailures() {
        LetterSearchTokenBackfillService service = mock(LetterSearchTokenBackfillService.class);
        LetterSearchTokenBackfillScheduler scheduler = scheduler(service);
        when(service.backfillNextBatch(anyLong(), eq(50))).thenReturn(
                batch(1, 0, 1, 10),
                batch(0, 0, 0, 10),
                batch(0, 0, 0, 0)
        );

        scheduler.backfill();
        scheduler.backfill();
        scheduler.backfill();

        org.mockito.InOrder order = inOrder(service);
        order.verify(service).backfillNextBatch(0, 50);
        order.verify(service).backfillNextBatch(10, 50);
        order.verify(service).backfillNextBatch(0, 50);
    }

    @Test
    void stopsAfterRetryPassLimitWhenFailurePersists() {
        LetterSearchTokenBackfillService service = mock(LetterSearchTokenBackfillService.class);
        LetterSearchTokenBackfillScheduler scheduler = scheduler(service);
        ReflectionTestUtils.setField(scheduler, "maxRetryPasses", 2);
        when(service.backfillNextBatch(anyLong(), eq(50))).thenReturn(
                batch(1, 0, 1, 10),
                batch(0, 0, 0, 10),
                batch(1, 0, 1, 10),
                batch(0, 0, 0, 10),
                batch(1, 0, 1, 10),
                batch(0, 0, 0, 10)
        );

        for (int attempt = 0; attempt < 7; attempt++) {
            scheduler.backfill();
        }

        verify(service, times(6)).backfillNextBatch(anyLong(), eq(50));
    }

    private LetterSearchTokenBackfillScheduler scheduler(
            LetterSearchTokenBackfillService service
    ) {
        LetterSearchTokenBackfillScheduler scheduler =
                new LetterSearchTokenBackfillScheduler(service);
        ReflectionTestUtils.setField(scheduler, "batchSize", 50);
        ReflectionTestUtils.setField(scheduler, "maxRetryPasses", 3);
        return scheduler;
    }

    private LetterSearchTokenBackfillService.BackfillBatchResult batch(
            int scanned,
            int indexed,
            int failed,
            long lastScannedId
    ) {
        return new LetterSearchTokenBackfillService.BackfillBatchResult(
                scanned,
                indexed,
                failed,
                lastScannedId
        );
    }
}
