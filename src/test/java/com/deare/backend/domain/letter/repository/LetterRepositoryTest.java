package com.deare.backend.domain.letter.repository;

import com.deare.backend.domain.folder.entity.Folder;
import com.deare.backend.domain.folder.repository.FolderRepository;
import com.deare.backend.domain.from.entity.From;
import com.deare.backend.domain.from.repository.FromRepository;
import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.entity.LetterSearchToken;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.entity.enums.Provider;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QuerydslConfig.class)
@ActiveProfiles("test")
class LetterRepositoryTest {
    @Autowired private UserRepository userRepository;
    @Autowired private FromRepository fromRepository;
    @Autowired private FolderRepository folderRepository;
    @Autowired private LetterRepository letterRepository;
    @Autowired private LetterSearchTokenRepository searchTokenRepository;
    @Autowired private EntityManager entityManager;

    @Test
    void findAvailableLettersReturnsOnlyLettersWithoutFolder() {
        User user = userRepository.save(User.signUpUser(
                Provider.GOOGLE, "folder-user", "folder-user@example.com", "folder-user"));
        From from = fromRepository.save(new From("sender", "#FFFFFF", "#000000", user));
        Folder firstFolder = folderRepository.save(Folder.create("first", 1, null, user));
        Folder secondFolder = folderRepository.save(Folder.create("second", 2, null, user));

        Letter unassignedLetter = letterRepository.save(createLetter("unassigned", user, from, null));
        letterRepository.save(createLetter("first-folder", user, from, firstFolder));
        letterRepository.save(createLetter("second-folder", user, from, secondFolder));

        Page<Letter> result = letterRepository.findAvailableLetters(
                user.getId(), null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Letter::getId)
                .containsExactly(unassignedLetter.getId());
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findLetterByIdAndOwnerRejectsOtherUserAndKeepsDeletedStateForOwner() {
        User owner = saveUser("letter-owner");
        User other = saveUser("letter-other");
        From from = fromRepository.save(new From("sender", "#FFFFFF", "#000000", owner));
        Letter letter = letterRepository.saveAndFlush(createLetter("owned-letter", owner, from, null));

        assertThat(letterRepository.findByIdAndUser_Id(letter.getId(), owner.getId()))
                .contains(letter);
        assertThat(letterRepository.findByIdAndUser_Id(letter.getId(), other.getId()))
                .isEmpty();

        letter.softDelete();
        letterRepository.flush();

        assertThat(letterRepository.findByIdAndUser_Id(letter.getId(), owner.getId()))
                .contains(letter);
        assertThat(letterRepository.findByIdAndUser_IdAndIsDeletedFalse(letter.getId(), owner.getId()))
                .isEmpty();
    }

    @Test
    void candidateFilterKeepsBlindIndexCandidatesAndUnindexedLettersForApplicationVerification() {
        User user = saveUser("candidate-owner");
        From from = fromRepository.save(new From("sender", "#FFFFFF", "#000000", user));
        Letter candidate = letterRepository.save(createLetter("needle candidate", user, from, null));
        Letter indexedButExcluded = letterRepository.save(createLetter("needle excluded", user, from, null));
        Letter unindexed = letterRepository.save(createLetter("needle legacy", user, from, null));
        Letter falsePositive = letterRepository.save(createLetter("different content", user, from, null));
        searchTokenRepository.save(LetterSearchToken.create(candidate, 1, "A".repeat(43)));
        searchTokenRepository.save(LetterSearchToken.create(indexedButExcluded, 1, "B".repeat(43)));
        searchTokenRepository.save(LetterSearchToken.create(falsePositive, 1, "C".repeat(43)));

        Page<Letter> result = letterRepository.findAvailableLetters(
                user.getId(),
                null,
                null,
                "needle",
                java.util.Set.of(candidate.getId(), falsePositive.getId()),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).extracting(Letter::getId)
                .containsExactlyInAnyOrder(candidate.getId(), unindexed.getId(), falsePositive.getId());
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void findActiveFromByIdAndOwnerRejectsOtherUserAndDeletedFrom() {
        User owner = saveUser("from-owner");
        User other = saveUser("from-other");
        From from = fromRepository.saveAndFlush(new From("sender", "#FFFFFF", "#000000", owner));

        assertThat(fromRepository.findByIdAndUser_IdAndIsDeletedFalse(from.getId(), owner.getId()))
                .contains(from);
        assertThat(fromRepository.findByIdAndUser_IdAndIsDeletedFalse(from.getId(), other.getId()))
                .isEmpty();

        from.softDelete();
        fromRepository.flush();

        assertThat(fromRepository.findByIdAndUser_IdAndIsDeletedFalse(from.getId(), owner.getId()))
                .isEmpty();
    }

    @Test
    void fetchesUserForRandomAndPinnedLetterContentDecryption() {
        User owner = saveUser("random-reader-owner");
        From from = fromRepository.save(new From("sender", "#FFFFFF", "#000000", owner));
        Letter random = letterRepository.saveAndFlush(
                createLetter("random-content", owner, from, null)
        );
        Letter pinned = createLetter("pinned-content", owner, from, null);
        pinned.updatePinned(true);
        letterRepository.saveAndFlush(pinned);
        entityManager.clear();

        Letter randomResult = letterRepository.findRandomLetterByUser(
                owner.getId(),
                0,
                LocalDateTime.now().plusMinutes(1)
        ).orElseThrow();
        assertThat(Hibernate.isInitialized(randomResult.getUser())).isTrue();
        assertThat(randomResult.getUser().getId()).isEqualTo(owner.getId());

        entityManager.clear();
        Letter pinnedResult = letterRepository.findPinnedLetterByUser(owner.getId())
                .orElseThrow();
        assertThat(Hibernate.isInitialized(pinnedResult.getUser())).isTrue();
        assertThat(pinnedResult.getUser().getId()).isEqualTo(owner.getId());
        assertThat(randomResult.getId()).isNotEqualTo(pinnedResult.getId());
    }

    @Test
    void fetchesUserForLetterListContentDecryption() {
        User owner = saveUser("list-reader-owner");
        From from = fromRepository.save(new From("sender", "#FFFFFF", "#000000", owner));
        Folder folder = folderRepository.save(Folder.create("folder", 1, null, owner));
        Letter assigned = letterRepository.saveAndFlush(
                createLetter("assigned-content", owner, from, folder)
        );
        Letter unassigned = letterRepository.saveAndFlush(
                createLetter("unassigned-content", owner, from, null)
        );
        entityManager.clear();

        Letter listResult = letterRepository.findLettersForList(
                owner.getId(), folder.getId(), null, null, null, null, PageRequest.of(0, 10)
        ).getContent().get(0);
        assertThat(listResult.getId()).isEqualTo(assigned.getId());
        assertThat(Hibernate.isInitialized(listResult.getUser())).isTrue();

        entityManager.clear();
        Letter unassignedResult = letterRepository.findAvailableLetters(
                owner.getId(), null, null, null, null, PageRequest.of(0, 10)
        ).getContent().get(0);
        assertThat(unassignedResult.getId()).isEqualTo(unassigned.getId());
        assertThat(Hibernate.isInitialized(unassignedResult.getUser())).isTrue();
    }

    private User saveUser(String suffix) {
        return userRepository.save(User.signUpUser(
                Provider.GOOGLE,
                "provider-" + suffix,
                suffix + "@example.com",
                suffix
        ));
    }

    private Letter createLetter(String content, User user, From from, Folder folder) {
        return new Letter(
                LocalDate.of(2026, 8, 26),
                content,
                1,
                user,
                from,
                folder
        );
    }
}
