-- ============================================================
-- V6: Create SaaS authentication, RBAC, subscription, session, and audit tables
-- ============================================================

CREATE TABLE users (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    email              VARCHAR(255) NOT NULL,
    password_hash      VARCHAR(255) NOT NULL,
    first_name         VARCHAR(100) NOT NULL,
    last_name          VARCHAR(100)          DEFAULT NULL,
    phone              VARCHAR(20)           DEFAULT NULL,
    email_verified     BOOLEAN      NOT NULL DEFAULT FALSE,
    verification_token VARCHAR(100)          DEFAULT NULL,
    reset_token        VARCHAR(100)          DEFAULT NULL,
    reset_token_expiry DATETIME(6)           DEFAULT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_verification ON users (verification_token);
CREATE INDEX idx_users_reset ON users (reset_token);

CREATE TABLE roles (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255)          DEFAULT NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uq_roles_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed Roles
INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'Platform Administrator with full access'),
('ROLE_MANAGER', 'Business Manager with access to rules and analytics'),
('ROLE_SUPPORT', 'Support Agent with access to customer messages'),
('ROLE_USER', 'Standard SaaS Customer User');

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,

    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE subscriptions (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    plan_type     VARCHAR(20)  NOT NULL DEFAULT 'TRIAL',
    status        VARCHAR(20)  NOT NULL DEFAULT 'TRIAL',
    start_date    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    end_date      DATETIME(6)  NOT NULL,
    auto_renew    BOOLEAN      NOT NULL DEFAULT TRUE,
    payment_ref   VARCHAR(100)          DEFAULT NULL,
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_subscriptions PRIMARY KEY (id),
    CONSTRAINT fk_subscriptions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_sub_plan CHECK (plan_type IN ('TRIAL', 'MONTHLY', 'YEARLY', 'LIFETIME')),
    CONSTRAINT chk_sub_status CHECK (status IN ('ACTIVE', 'TRIAL', 'EXPIRED', 'SUSPENDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_subscriptions_user_status ON subscriptions (user_id, status);

CREATE TABLE user_sessions (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    refresh_token VARCHAR(255) NOT NULL,
    ip_address    VARCHAR(45)           DEFAULT NULL,
    user_agent    VARCHAR(255)          DEFAULT NULL,
    revoked       BOOLEAN      NOT NULL DEFAULT FALSE,
    expires_at    DATETIME(6)  NOT NULL,
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_user_sessions PRIMARY KEY (id),
    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_user_sessions_refresh UNIQUE (refresh_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_sessions_refresh ON user_sessions (refresh_token);

CREATE TABLE audit_logs (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT                DEFAULT NULL,
    action      VARCHAR(100) NOT NULL,
    resource    VARCHAR(100) NOT NULL,
    details     TEXT                  DEFAULT NULL,
    ip_address  VARCHAR(45)           DEFAULT NULL,
    timestamp   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_audit_logs PRIMARY KEY (id),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_audit_logs_user ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs (action);
