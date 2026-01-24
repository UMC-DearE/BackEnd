package com.deare.backend.domain.letter.entity;

import com.deare.backend.domain.image.entity.Image;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="letter_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LetterImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="letter_image_id", nullable=false)
    private Long id;

    @Column(name="image_order", nullable = false)
    private int imageOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="letter_id", nullable = false)
    private Letter letter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="image_id", nullable = false)
    private Image image;

    void setLetter(Letter letter){
        this.letter = letter;
    }
}
