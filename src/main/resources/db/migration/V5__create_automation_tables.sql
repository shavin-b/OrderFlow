-- ============================================================
-- V5: Create automation, business hours, greetings, and queued messages tables
-- ============================================================

CREATE TABLE automation_rules (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    name             VARCHAR(255)   NOT NULL,
    description      TEXT                    DEFAULT NULL,
    priority         INT            NOT NULL DEFAULT 0,
    active           BOOLEAN        NOT NULL DEFAULT TRUE,
    cooldown_seconds INT            NOT NULL DEFAULT 0,
    trigger_count    BIGINT         NOT NULL DEFAULT 0,
    created_at       DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_automation_rules PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_automation_rules_active_priority ON automation_rules (active, priority DESC);

CREATE TABLE keywords (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    automation_rule_id BIGINT       NOT NULL,
    pattern            VARCHAR(255) NOT NULL,
    match_type         VARCHAR(20)  NOT NULL DEFAULT 'CONTAINS',
    ignore_case        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_keywords PRIMARY KEY (id),
    CONSTRAINT fk_keywords_automation_rule
        FOREIGN KEY (automation_rule_id) REFERENCES automation_rules (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_keywords_match_type CHECK (match_type IN ('CONTAINS', 'EXACT', 'STARTS_WITH', 'ENDS_WITH', 'REGEX'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_keywords_rule ON keywords (automation_rule_id);

CREATE TABLE replies (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    automation_rule_id BIGINT       NOT NULL,
    message_body       TEXT         NOT NULL,
    reply_order        INT          NOT NULL DEFAULT 0,
    delay_seconds      INT          NOT NULL DEFAULT 0,
    media_url          TEXT                  DEFAULT NULL,
    media_type         VARCHAR(50)           DEFAULT NULL,
    created_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_replies PRIMARY KEY (id),
    CONSTRAINT fk_replies_automation_rule
        FOREIGN KEY (automation_rule_id) REFERENCES automation_rules (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_replies_rule_order ON replies (automation_rule_id, reply_order ASC);

CREATE TABLE business_hours (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    day_of_week  VARCHAR(15)  NOT NULL,
    start_time   TIME         NOT NULL,
    end_time     TIME         NOT NULL,
    timezone     VARCHAR(50)  NOT NULL DEFAULT 'UTC',
    enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    away_message TEXT                  DEFAULT NULL,
    created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_business_hours PRIMARY KEY (id),
    CONSTRAINT uq_business_hours_day UNIQUE (day_of_week)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE greetings (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    name         VARCHAR(255) NOT NULL,
    message_body TEXT         NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    media_url    TEXT                  DEFAULT NULL,
    media_type   VARCHAR(50)           DEFAULT NULL,
    created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_greetings PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE queued_messages (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    conversation_id BIGINT       NOT NULL,
    recipient_phone VARCHAR(20)  NOT NULL,
    message_body    TEXT         NOT NULL,
    media_url       TEXT                  DEFAULT NULL,
    media_type      VARCHAR(50)           DEFAULT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count     INT          NOT NULL DEFAULT 0,
    max_retries     INT          NOT NULL DEFAULT 3,
    error_message   TEXT                  DEFAULT NULL,
    scheduled_at    DATETIME(6)  NOT NULL,
    processed_at    DATETIME(6)           DEFAULT NULL,
    idempotency_key VARCHAR(100)          DEFAULT NULL,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_queued_messages PRIMARY KEY (id),
    CONSTRAINT fk_queued_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_queued_idempotency UNIQUE (idempotency_key),
    CONSTRAINT chk_queued_status CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'FAILED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_queued_status_scheduled ON queued_messages (status, scheduled_at);
