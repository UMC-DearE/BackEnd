package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.exception.LetterErrorCode;
import com.deare.backend.domain.letter.repository.LetterSearchTokenRepository;
import com.deare.backend.domain.letter.search.BlindIndexKeyProvider;
import com.deare.backend.domain.letter.search.BlindIndexKeyVersion;
import com.deare.backend.domain.letter.search.VersionedBlindIndexKey;
import com.deare.backend.global.common.exception.GeneralException;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LetterSearchCandidateResolverTest {

    private final LetterSearchTokenRepository repository = mock(LetterSearchTokenRepository.class);
    private final BlindIndexKeyProvider keyProvider = mock(BlindIndexKeyProvider.class);

    @Test
    void mergesCandidatesFromEveryReadableKeyVersion() {
        LetterSearchCandidateResolver resolver = enabledResolver();
        when(keyProvider.readableKeys(1L)).thenReturn(List.of(versionedKey(2), versionedKey(1)));
        when(repository.findCandidateLetterIds(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(2),
                anySet(),
                org.mockito.ArgumentMatchers.eq(1_001)
        )).thenReturn(List.of(10L, 20L));
        when(repository.findCandidateLetterIds(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(1),
                anySet(),
                org.mockito.ArgumentMatchers.eq(1_001)
        )).thenReturn(List.of(20L, 30L));

        assertThat(resolver.resolve(1L, "search"))
                .contains(Set.of(10L, 20L, 30L));
    }

    @Test
    void fallsBackWhenKeyProviderIsDisabled() {
        LetterSearchCandidateResolver disabled = new LetterSearchCandidateResolver(
                repository,
                Optional.empty()
        );

        assertThat(disabled.resolve(1L, "search")).isEmpty();
        verify(repository, never()).findCandidateLetterIds(
                org.mockito.ArgumentMatchers.anyLong(),
                anyInt(),
                anySet(),
                anyInt()
        );
    }

    @Test
    void rejectsSingleCodePointKeywordAfterNormalization() {
        LetterSearchCandidateResolver resolver = enabledResolver();

        assertThatThrownBy(() -> resolver.resolve(1L, "  Ａ  "))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(LetterErrorCode.INVALID_REQUEST)
                );

        verify(keyProvider, never()).readableKeys(org.mockito.ArgumentMatchers.anyLong());
        verify(repository, never()).findCandidateLetterIds(
                org.mockito.ArgumentMatchers.anyLong(),
                anyInt(),
                anySet(),
                anyInt()
        );
    }

    @Test
    void treatsBlankKeywordAsNoSearch() {
        assertThat(enabledResolver().resolve(1L, "   ")).isEmpty();

        verify(keyProvider, never()).readableKeys(org.mockito.ArgumentMatchers.anyLong());
        verify(repository, never()).findCandidateLetterIds(
                org.mockito.ArgumentMatchers.anyLong(),
                anyInt(),
                anySet(),
                anyInt()
        );
    }

    @Test
    void fallsBackWhenCandidateCountExceedsQueryBoundary() {
        LetterSearchCandidateResolver resolver = enabledResolver();
        when(keyProvider.readableKeys(1L)).thenReturn(List.of(versionedKey(1)));
        when(repository.findCandidateLetterIds(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(1),
                anySet(),
                org.mockito.ArgumentMatchers.eq(1_001)
        )).thenReturn(LongStream.rangeClosed(1, 1_001).boxed().toList());

        assertThat(resolver.resolve(1L, "search")).isEmpty();
    }

    @Test
    void rejectsKeywordOverCodePointLimitBeforeKeyOrTokenLookup() {
        LetterSearchCandidateResolver resolver = enabledResolver();
        String keyword = Character.toString(0x1F600).repeat(102);

        assertThatThrownBy(() -> resolver.resolve(1L, keyword))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(LetterErrorCode.INVALID_REQUEST)
                );

        verify(keyProvider, never()).readableKeys(org.mockito.ArgumentMatchers.anyLong());
        verify(repository, never()).findCandidateLetterIds(
                org.mockito.ArgumentMatchers.anyLong(),
                anyInt(),
                anySet(),
                anyInt()
        );
    }

    @Test
    void acceptsKeywordAtCodePointLimit() {
        LetterSearchCandidateResolver resolver = enabledResolver();
        when(keyProvider.readableKeys(1L)).thenReturn(List.of(versionedKey(1)));

        resolver.resolve(1L, Character.toString(97).repeat(101));

        verify(repository).findCandidateLetterIds(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(1),
                anySet(),
                org.mockito.ArgumentMatchers.eq(1_001)
        );
    }

    @Test
    void usesServerResolvedUserIdForKeyAndCandidateLookup() {
        LetterSearchCandidateResolver resolver = enabledResolver();
        when(keyProvider.readableKeys(7L)).thenReturn(List.of(versionedKey(1)));

        resolver.resolve(7L, "search");

        verify(keyProvider).readableKeys(7L);
        verify(repository).findCandidateLetterIds(
                org.mockito.ArgumentMatchers.eq(7L),
                org.mockito.ArgumentMatchers.eq(1),
                anySet(),
                org.mockito.ArgumentMatchers.eq(1_001)
        );
    }

    private LetterSearchCandidateResolver enabledResolver() {
        return new LetterSearchCandidateResolver(repository, Optional.of(keyProvider));
    }

    private VersionedBlindIndexKey versionedKey(int version) {
        return new VersionedBlindIndexKey(
                new BlindIndexKeyVersion(version),
                new SecretKeySpec(
                        "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8),
                        "HmacSHA256"
                )
        );
    }
}
