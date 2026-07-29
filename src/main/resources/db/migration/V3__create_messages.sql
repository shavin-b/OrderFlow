-- ============================================================
-- V3: Create messages table
-- ============================================================
CREATE TABLE messages (
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    conversation_id   BIGINT         NOT NULL,
    wa_message_id     VARCHAR(100)            DEFAULT NULL,
    direction         VARCHAR(10)    NOT NULL,
    type              VARCHAR(20)    NOT NULL,
    body              TEXT                    DEFAULT NULL,
    status            VARCHAR(20)    NOT NULL DEFAULT 'SENT',
    timestamp         DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_messages PRIMARY KEY (id),
    CONSTRAINT fk_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT uq_messages_wa_id UNIQUE (wa_message_id),
    CONSTRAINT chk_messages_direction CHECK (direction IN ('INBOUND', 'OUTBOUND')),
    CONSTRAINT chk_messages_type CHECK (type IN (
        'TEXT', 'IMAGE', 'DOCUMENT', 'VIDEO', 'AUDIO',
        'INTERACTIVE', 'TEMPLATE', 'STICKER', 'LOCATION', 'UNKNOWN'
    )),
    CONSTRAINT chk_messages_status CHECK (status IN (
        'SENT', 'DELIVERED', 'READ', 'FAILED', 'PENDING', 'RECEIVED'
    ))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_messages_conversation ON messages (conversation_id);
CREATE INDEX idx_messages_wa_id        ON messages (wa_message_id);
CREATE INDEX idx_messages_direction    ON messages (direction);
CREATE INDEX idx_messages_status       ON messages (status);
CREATE INDEX idx_messages_timestamp    ON messages (timestamp);
