package com.deare.backend.domain.invite.entity;

import com.deare.backend.domain.user.entity.User;
import com.deare.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_invite_code",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_invite_code_user", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_user_invite_code_code", columnNames = "invite_code")
        }
)
public class UserInviteCode extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_invite_code_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "invite_code", nullable = false, length = 64)
    private String inviteCode;

    public static UserInviteCode create(User user, String inviteCode) {
        UserInviteCode userInviteCode = new UserInviteCode();
        userInviteCode.user = user;
        userInviteCode.inviteCode = inviteCode;
        return userInviteCode;
    }
}
