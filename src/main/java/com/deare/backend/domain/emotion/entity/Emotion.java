package com.deare.backend.domain.emotion.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="emotions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Emotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="emotion_id")
    private Long id;

    @Column(name="name", nullable = false, length = 20)
    private String name;

    @ManyToOne(fetch=FetchType.LAZY, optional = false)
    @JoinColumn(name="category_id", nullable = false)
    private EmotionCategory emotionCategory;

    private Emotion(String name, EmotionCategory emotionCategory) {
        this.name = name;
        this.emotionCategory = emotionCategory;
    }

    public static Emotion create(String name, EmotionCategory emotionCategory) {
        return new Emotion(name, emotionCategory);
    }
}
