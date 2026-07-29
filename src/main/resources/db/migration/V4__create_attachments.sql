-- ============================================================
-- V4: Create attachments table
-- ============================================================
CREATE TABLE attachments (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    message_id    BIGINT         NOT NULL,
    media_id      VARCHAR(100)   NOT NULL,
    media_url     TEXT                    DEFAULT NULL,
    mime_type     VARCHAR(100)            DEFAULT NULL,
    sha256        VARCHAR(100)            DEFAULT NULL,
    file_size     BIGINT                  DEFAULT NULL,
    file_name     VARCHAR(255)            DEFAULT NULL,
    caption       TEXT                    DEFAULT NULL,
    created_at    DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_attachments PRIMARY KEY (id),
    CONSTRAINT fk_attachments_message
        FOREIGN KEY (message_id) REFERENCES messages (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_attachments_message  ON attachments (message_id);
CREATE INDEX idx_attachments_media_id ON attachments (media_id);
