CREATE TABLE letter_search_token (
    letter_search_token_id BIGINT      NOT NULL AUTO_INCREMENT,
    letter_id              BIGINT      NOT NULL,
    index_key_version      INT         NOT NULL,
    token                  VARCHAR(43) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    PRIMARY KEY (letter_search_token_id),
    UNIQUE KEY uk_letter_search_token (letter_id, index_key_version, token),
    KEY idx_letter_search_token_candidate (index_key_version, token, letter_id),
    CONSTRAINT fk_letter_search_token_letter
        FOREIGN KEY (letter_id) REFERENCES letter (letter_id) ON DELETE CASCADE,
    CONSTRAINT chk_letter_search_token_key_version CHECK (index_key_version > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
