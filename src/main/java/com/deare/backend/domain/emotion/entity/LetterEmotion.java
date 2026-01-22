package com.deare.backend.domain.emotion.entity;

import com.deare.backend.domain.letter.entity.Letter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name="letter_emotion",
        uniqueConstraints = {
                @UniqueConstraint(
                        name="uq_letter_emotion",
                        columnNames = {"letter_id", "emotion_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LetterEmotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="letter_emotion_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="letter_id", nullable = false)
    private Letter letter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="emotion_id", nullable = false)
    private Emotion emotion;

    private LetterEmotion(Letter letter, Emotion emotion) {
        this.letter = letter;
        this.emotion = emotion;
    }
}
