ALTER TABLE letter
    ADD COLUMN content_ciphertext LONGTEXT NULL AFTER content,
    ADD COLUMN content_encryption_nonce VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NULL
        AFTER content_ciphertext,
    ADD COLUMN content_encryption_key_version INT NULL AFTER content_encryption_nonce,
    ADD COLUMN content_encryption_format_version SMALLINT NULL
        AFTER content_encryption_key_version,
    ADD CONSTRAINT chk_letter_content_encryption_state CHECK (
        (
            content_ciphertext IS NULL
            AND content_encryption_nonce IS NULL
            AND content_encryption_key_version IS NULL
            AND content_encryption_format_version IS NULL
        )
        OR
        (
            content_ciphertext IS NOT NULL
            AND CHAR_LENGTH(content_ciphertext) > 0
            AND content_encryption_nonce IS NOT NULL
            AND CHAR_LENGTH(content_encryption_nonce) = 16
            AND content_encryption_key_version > 0
            AND content_encryption_format_version = 1
        )
    );
