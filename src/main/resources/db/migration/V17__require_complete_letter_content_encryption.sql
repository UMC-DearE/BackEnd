ALTER TABLE letter
    MODIFY COLUMN content_ciphertext LONGTEXT NOT NULL,
    MODIFY COLUMN content_encryption_nonce VARCHAR(16)
        CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    MODIFY COLUMN content_encryption_key_version INT NOT NULL,
    MODIFY COLUMN content_encryption_format_version INT NOT NULL,
    DROP COLUMN content;
