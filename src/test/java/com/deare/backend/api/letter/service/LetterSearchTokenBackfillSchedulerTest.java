package com.deare.backend.api.letter.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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
}
