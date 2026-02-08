package com.deare.backend.domain.setting.entity;

import com.deare.backend.domain.user.entity.User;
import com.deare.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_setting")
public class UserSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_setting_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false)
    private Theme theme = Theme.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "font", nullable = false)
    private Font font = Font.PRETENDARD;

    @Column(name = "home_color", nullable = false, length = 16)
    private String homeColor;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_plan", nullable = false)
    private MembershipPlan membershipPlan = MembershipPlan.FREE;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public void updateFont(Font font) {
        this.font = font;
    }

    public boolean isPlus() {
        return this.membershipPlan == MembershipPlan.PLUS;
    }

    public void upgradeToPlus() {
        this.membershipPlan = MembershipPlan.PLUS;
    }
    public static UserSetting createDefault(User user, String homeColor) {
        UserSetting us = new UserSetting();
        us.user = user;
        us.homeColor = homeColor;
        return us;
    }
    public void updateHomeColor(String homeColor) {
        this.homeColor = homeColor;
    }
}
