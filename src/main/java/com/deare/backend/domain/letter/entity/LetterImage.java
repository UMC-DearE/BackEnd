package com.deare.backend.domain.letter.entity;

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

    /**
     * 현재 Image 엔티티 구성이 없어서 String으로만 넣어둠
     * Image 구현 후 매핑 필요
     */
    //@ManyToOne(fetch = FetchType.LAZY, optional = false)
    //@JoinColumn(name="image_id", nullable = false)
    @Column(name="image_id",nullable=false)
    private String image;

    void setLetter(Letter letter){
        this.letter = letter;
    }
}
