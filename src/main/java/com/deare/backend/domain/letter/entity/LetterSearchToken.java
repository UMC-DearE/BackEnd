package com.deare.backend.domain.letter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "letter_search_token",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_letter_search_token",
                columnNames = {"letter_id", "index_key_version", "token"}
        ),
        indexes = @Index(
                name = "idx_letter_search_token_candidate",
                columnList = "index_key_version, token, letter_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LetterSearchToken {

    private static final int TOKEN_LENGTH = 43;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "letter_search_token_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "letter_id", nullable = false)
    private Letter letter;

    @Column(name = "index_key_version", nullable = false)
    private int indexKeyVersion;

    @Column(name = "token", nullable = false, length = TOKEN_LENGTH)
    private String token;

    public static LetterSearchToken create(Letter letter, int indexKeyVersion, String token) {
        if (letter == null) {
            throw new IllegalArgumentException("Letter is required.");
        }
        if (indexKeyVersion <= 0) {
            throw new IllegalArgumentException("Index key version must be positive.");
        }
        if (token == null || token.length() != TOKEN_LENGTH) {
            throw new IllegalArgumentException("Blind index token must be 43 characters.");
        }

        LetterSearchToken searchToken = new LetterSearchToken();
        searchToken.letter = letter;
        searchToken.indexKeyVersion = indexKeyVersion;
        searchToken.token = token;
        return searchToken;
    }
}
