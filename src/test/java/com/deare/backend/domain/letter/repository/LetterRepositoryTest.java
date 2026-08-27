package com.deare.backend.domain.letter.repository;

import com.deare.backend.domain.folder.entity.Folder;
import com.deare.backend.domain.folder.repository.FolderRepository;
import com.deare.backend.domain.from.entity.From;
import com.deare.backend.domain.from.repository.FromRepository;
import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.entity.enums.Provider;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.config.QuerydslConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QuerydslConfig.class)
@ActiveProfiles("test")
class LetterRepositoryTest {
    @Autowired private UserRepository userRepository;
    @Autowired private FromRepository fromRepository;
    @Autowired private FolderRepository folderRepository;
    @Autowired private LetterRepository letterRepository;

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
                user.getId(), null, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Letter::getId)
                .containsExactly(unassignedLetter.getId());
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    private Letter createLetter(String content, User user, From from, Folder folder) {
        return new Letter(
                content,
                LocalDate.of(2026, 8, 26),
                content,
                1,
                content + "-hash",
                user,
                from,
                folder
        );
    }
}
