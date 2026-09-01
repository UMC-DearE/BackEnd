ALTER TABLE letter
    MODIFY COLUMN content_ciphertext LONGTEXT NULL,
    MODIFY COLUMN content_encryption_nonce VARCHAR(16)
        CHARACTER SET ascii COLLATE ascii_bin NULL,
    MODIFY COLUMN content_encryption_key_version INT NULL,
    MODIFY COLUMN content_encryption_format_version INT NULL;
