package com.deare.backend.domain.from.entity;

import com.deare.backend.domain.user.entity.User;
import com.deare.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="user_from")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class From extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_from_id")
    private Long id;

    @Column(name="from_name", nullable = false, length = 7)
    private String name;

    @Column(name="from_bg_color", nullable = false, length = 16)
    private String backgroundColor;

    @Column(name="from_font_color", nullable = false, length = 16)
    private String fontColor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    public From(
            String name,
            String backgroundColor,
            String fontColor,
            User user
    ){
        this.name = name;
        this.backgroundColor = backgroundColor;
        this.fontColor = fontColor;
        this.user = user;
    }


    public void changeFromName(String name){
        this.name = name;
    }

    public void changeColors(String backgroundColor, String fontColor){
        this.backgroundColor = backgroundColor;
        this.fontColor = fontColor;
    }
}
