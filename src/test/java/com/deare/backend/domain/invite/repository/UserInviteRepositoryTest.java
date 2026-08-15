package com.deare.backend.domain.invite.repository;

import com.deare.backend.domain.invite.entity.UserInviteCode;
import com.deare.backend.domain.invite.entity.UserInviteHistory;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.entity.enums.Provider;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.config.QuerydslConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(QuerydslConfig.class)
@ActiveProfiles("test")
class UserInviteRepositoryTest {

    @Autowired private UserRepository userRepository;
    @Autowired private UserInviteCodeRepository userInviteCodeRepository;
    @Autowired private UserInviteHistoryRepository userInviteHistoryRepository;

    @Test
    void findsInviteCodeByUserAndCode() {
        User user = saveUser("provider-1", "user1@example.com", "user1");
        UserInviteCode code = userInviteCodeRepository.save(UserInviteCode.create(user, "code-1"));

        assertThat(userInviteCodeRepository.findByUserId(user.getId())).contains(code);
        assertThat(userInviteCodeRepository.findByInviteCode("code-1")).contains(code);
    }

    @Test
    void rejectsDuplicateInviteCode() {
        User first = saveUser("provider-1", "user1@example.com", "user1");
        User second = saveUser("provider-2", "user2@example.com", "user2");
        userInviteCodeRepository.saveAndFlush(UserInviteCode.create(first, "same-code"));

        assertThatThrownBy(() -> userInviteCodeRepository.saveAndFlush(
                UserInviteCode.create(second, "same-code")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsMultipleInviteCodesForSameUser() {
        User user = saveUser("provider-1", "user1@example.com", "user1");
        userInviteCodeRepository.saveAndFlush(UserInviteCode.create(user, "code-1"));

        assertThatThrownBy(() -> userInviteCodeRepository.saveAndFlush(
                UserInviteCode.create(user, "code-2")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicateInviteHistoryForInvitee() {
        User firstInviter = saveUser("provider-1", "user1@example.com", "user1");
        User secondInviter = saveUser("provider-2", "user2@example.com", "user2");
        User invitee = saveUser("provider-3", "user3@example.com", "user3");
        userInviteHistoryRepository.saveAndFlush(UserInviteHistory.create(firstInviter, invitee));

        assertThat(userInviteHistoryRepository.existsByInviteeId(invitee.getId())).isTrue();
        assertThatThrownBy(() -> userInviteHistoryRepository.saveAndFlush(
                UserInviteHistory.create(secondInviter, invitee)
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    private User saveUser(String providerId, String email, String nickname) {
        return userRepository.saveAndFlush(
                User.signUpUser(Provider.GOOGLE, providerId, email, nickname)
        );
    }
}
