package com.deare.backend.domain.emotion.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="emotion_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmotionCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="category_id")
    private Long id;

    @Column(name="type", nullable = false)
    private String type;

    @Column(name="bg_color", nullable = false)
    private String bgColor;

    @Column(name="font_color", nullable = false)
    private String fontColor;

    public EmotionCategory(String type, String bgColor, String fontColor) {
        this.type = type;
        this.bgColor = bgColor;
        this.fontColor = fontColor;
    }
}
