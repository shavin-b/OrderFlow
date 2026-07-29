-- ============================================================
-- V2: Create conversations table
-- ============================================================
CREATE TABLE conversations (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    customer_id   BIGINT         NOT NULL,
    status        VARCHAR(20)    NOT NULL DEFAULT 'OPEN',
    opened_at     DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    closed_at     DATETIME(6)             DEFAULT NULL,
    created_at    DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_conversations PRIMARY KEY (id),
    CONSTRAINT fk_conversations_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_conversations_status CHECK (status IN ('OPEN', 'CLOSED', 'PENDING'))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_conversations_customer ON conversations (customer_id);
CREATE INDEX idx_conversations_status   ON conversations (status);
CREATE INDEX idx_conversations_opened   ON conversations (opened_at);
