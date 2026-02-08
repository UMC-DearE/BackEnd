package com.deare.backend.domain.user.entity;

import com.deare.backend.global.common.entity.BaseEntity;
import com.deare.backend.domain.image.entity.Image;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "nickname", nullable = false, length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.ROLE_USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private Provider provider;

    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "intro", nullable = true, length = 50)
    private String intro;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    /**
     * API (/auth/signup) 회원 가입
     * TO-DO : service 로직에서 User-Terms 매핑하기
     * @param provider 소셜 프로바이더(<- signup-token)
     * @param providerId 소셜 프로바이더 아이디(<- signup-token)
     * @param email 소셜 가입 아이디(<- signup-token)
     * @param nickname (<-api/auth/signup)
     * @return DB 저장할 User
     */
    public static User signUpUser(
            Provider provider, String providerId, String email, String nickname
    ) {
        User user = new User();

        user.provider = provider;
        user.providerId = providerId;
        user.email = email;
        user.nickname = nickname;

        // role, status -> Default Value
        return user;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateIntro(String intro) {
        this.intro = intro;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    /**
     * 회원 탈퇴 (소프트 딜리트)
     * - status를 INACTIVE로 변경
     * - BaseEntity의 softDelete() 호출 (isDeleted=true, deletedAt=now)
     */
    public void deactivate() {
        this.status = Status.INACTIVE;
        this.softDelete();
    }

    /**
     * 회원 복구 (삭제 중인 유저가 다시 로그인 시)
     * - status를 ACTIVE로 변경
     * - BaseEntity의 restore() 호출 (isDeleted=false, deletedAt=null)
     */
    public void reactivate() {
        this.status = Status.ACTIVE;
        this.restore();
    }

}
