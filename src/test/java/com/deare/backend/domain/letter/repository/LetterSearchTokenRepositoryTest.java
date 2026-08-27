package com.deare.backend.domain.letter.repository;

import com.deare.backend.domain.from.entity.From;
import com.deare.backend.domain.from.repository.FromRepository;
import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.entity.LetterSearchToken;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.entity.enums.Provider;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.config.QuerydslConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(QuerydslConfig.class)
@ActiveProfiles("test")
class LetterSearchTokenRepositoryTest {

    private static final String TOKEN_A = "A".repeat(43);
    private static final String TOKEN_B = "B".repeat(43);
    private static final String TOKEN_C = "C".repeat(43);

    @Autowired private UserRepository userRepository;
    @Autowired private FromRepository fromRepository;
    @Autowired private LetterRepository letterRepository;
    @Autowired private LetterSearchTokenRepository searchTokenRepository;

    @Test
    void findsOnlyLettersContainingEveryTokenForUserAndKeyVersion() {
        User owner = saveUser("owner");
        User other = saveUser("other");
        Letter exact = saveLetter(owner, "exact");
        Letter partial = saveLetter(owner, "partial");
        Letter otherUsers = saveLetter(other, "other-users");

        saveTokens(exact, 1, TOKEN_A, TOKEN_B);
        saveTokens(partial, 1, TOKEN_A, TOKEN_C);
        saveTokens(otherUsers, 1, TOKEN_A, TOKEN_B);

        assertThat(searchTokenRepository.findCandidateLetterIds(
                owner.getId(), 1, Set.of(TOKEN_A, TOKEN_B), 100
        )).containsExactly(exact.getId());
    }

    @Test
    void keepsKeyVersionsIsolated() {
        User owner = saveUser("owner");
        Letter oldVersion = saveLetter(owner, "old-version");
        Letter currentVersion = saveLetter(owner, "current-version");
        saveTokens(oldVersion, 1, TOKEN_A, TOKEN_B);
        saveTokens(currentVersion, 2, TOKEN_A, TOKEN_B);

        assertThat(searchTokenRepository.findCandidateLetterIds(
                owner.getId(), 2, Set.of(TOKEN_A, TOKEN_B), 100
        )).containsExactly(currentVersion.getId());
    }

    @Test
    void returnsEmptyForInvalidSearchBoundary() {
        assertThat(searchTokenRepository.findCandidateLetterIds(1L, 1, Set.of(), 100)).isEmpty();
        assertThat(searchTokenRepository.findCandidateLetterIds(null, 1, Set.of(TOKEN_A), 100)).isEmpty();
        assertThat(searchTokenRepository.findCandidateLetterIds(1L, 0, Set.of(TOKEN_A), 100)).isEmpty();
        assertThat(searchTokenRepository.findCandidateLetterIds(1L, 1, Set.of(TOKEN_A), 0)).isEmpty();
    }

    @Test
    void rejectsDuplicateTokenForSameLetterAndVersion() {
        User owner = saveUser("owner");
        Letter letter = saveLetter(owner, "duplicate");
        searchTokenRepository.saveAndFlush(LetterSearchToken.create(letter, 1, TOKEN_A));

        assertThatThrownBy(() -> searchTokenRepository.saveAndFlush(
                LetterSearchToken.create(letter, 1, TOKEN_A)
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletesEveryKeyVersionForLetter() {
        User owner = saveUser("delete-owner");
        Letter target = saveLetter(owner, "delete-target");
        Letter untouched = saveLetter(owner, "delete-untouched");
        saveTokens(target, 1, TOKEN_A);
        saveTokens(target, 2, TOKEN_B);
        saveTokens(untouched, 1, TOKEN_C);

        searchTokenRepository.deleteAllByLetterId(target.getId());
        searchTokenRepository.flush();

        assertThat(searchTokenRepository.findAll())
                .extracting(token -> token.getLetter().getId())
                .containsExactly(untouched.getId());
    }

    private User saveUser(String suffix) {
        return userRepository.saveAndFlush(User.signUpUser(
                Provider.GOOGLE,
                "provider-" + suffix,
                suffix + "@example.com",
                suffix
        ));
    }

    private Letter saveLetter(User user, String content) {
        From from = fromRepository.saveAndFlush(new From(
                "from",
                "#FFFFFF",
                "#000000",
                user
        ));
        return letterRepository.saveAndFlush(new Letter(
                content,
                LocalDate.of(2026, 8, 26),
                "summary",
                1,
                "0".repeat(64),
                user,
                from,
                null
        ));
    }

    private void saveTokens(Letter letter, int version, String... tokens) {
        searchTokenRepository.saveAllAndFlush(
                List.of(tokens).stream()
                        .map(token -> LetterSearchToken.create(letter, version, token))
                        .toList()
        );
    }
}
