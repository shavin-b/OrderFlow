-- ============================================================
-- V1: Create customers table
-- ============================================================
CREATE TABLE customers (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    wa_id         VARCHAR(20)    NOT NULL,
    phone         VARCHAR(20)    NOT NULL,
    name          VARCHAR(255)   NOT NULL DEFAULT 'Unknown',
    email         VARCHAR(255)            DEFAULT NULL,
    status        VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT uq_customers_wa_id UNIQUE (wa_id),
    CONSTRAINT uq_customers_phone UNIQUE (phone),
    CONSTRAINT chk_customers_status CHECK (status IN ('ACTIVE', 'BLOCKED', 'INACTIVE'))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_customers_phone   ON customers (phone);
CREATE INDEX idx_customers_status  ON customers (status);
CREATE INDEX idx_customers_created ON customers (created_at);
