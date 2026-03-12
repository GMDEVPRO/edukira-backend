-- ══════════════════════════════════════════════════════
-- Edukira — Migração inicial
-- V1 · Criação de todas as tabelas
-- ══════════════════════════════════════════════════════

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- SCHOOLS
CREATE TABLE schools (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    country             CHAR(3) NOT NULL,
    city                VARCHAR(100),
    type                VARCHAR(20) NOT NULL,
    default_language    VARCHAR(5) NOT NULL DEFAULT 'fr',
    wave_api_key        TEXT,
    orange_api_key      TEXT,
    mtn_subscription_key TEXT,
    africas_talking_key TEXT,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- USER PROFILES
CREATE TABLE user_profiles (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id           UUID NOT NULL REFERENCES schools(id),
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    password_hash       TEXT NOT NULL,
    role                VARCHAR(20) NOT NULL,
    preferred_language  VARCHAR(5) NOT NULL DEFAULT 'fr',
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_login          TIMESTAMPTZ,
    UNIQUE (email, school_id)
);

-- STUDENTS
CREATE TABLE students (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id           UUID NOT NULL REFERENCES schools(id),
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    class_level         VARCHAR(50),
    guardian_name       VARCHAR(200),
    guardian_phone      VARCHAR(30),
    guardian_email      VARCHAR(255),
    guardian_language   VARCHAR(5) DEFAULT 'fr',
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    enrollment_date     DATE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- GRADES
CREATE TABLE grades (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID NOT NULL REFERENCES students(id),
    school_id       UUID NOT NULL REFERENCES schools(id),
    subject_name    VARCHAR(100) NOT NULL,
    grade1          NUMERIC(5,2),
    grade2          NUMERIC(5,2),
    average         NUMERIC(5,2),
    coefficient     NUMERIC(5,2) DEFAULT 1,
    period          VARCHAR(15) NOT NULL,
    year            VARCHAR(10) NOT NULL,
    published       BOOLEAN NOT NULL DEFAULT FALSE,
    published_at    TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- PAYMENTS
CREATE TABLE payments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID NOT NULL REFERENCES students(id),
    school_id       UUID NOT NULL REFERENCES schools(id),
    amount          NUMERIC(12,2) NOT NULL,
    currency        VARCHAR(5) NOT NULL DEFAULT 'XOF',
    month           VARCHAR(7) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    method          VARCHAR(20) NOT NULL,
    session_id      TEXT,
    transaction_id  TEXT,
    receipt_url     TEXT,
    due_date        TIMESTAMPTZ,
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- MESSAGES
CREATE TABLE messages (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id           UUID NOT NULL REFERENCES schools(id),
    sender_id           UUID NOT NULL REFERENCES user_profiles(id),
    recipient_phone     VARCHAR(30),
    recipient_email     VARCHAR(255),
    body                TEXT NOT NULL,
    channel             VARCHAR(20) NOT NULL,
    language            VARCHAR(5) NOT NULL DEFAULT 'fr',
    status              VARCHAR(20) NOT NULL DEFAULT 'SENT',
    external_message_id TEXT,
    sent_at             TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- REFRESH TOKENS
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES user_profiles(id),
    token       TEXT NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE
);

-- ── ÍNDICES ──────────────────────────────────────────
CREATE INDEX idx_students_school  ON students(school_id);
CREATE INDEX idx_students_class   ON students(school_id, class_level);
CREATE INDEX idx_grades_student   ON grades(student_id, period, year);
CREATE INDEX idx_payments_school  ON payments(school_id, status);
CREATE INDEX idx_payments_session ON payments(session_id);
CREATE INDEX idx_messages_school  ON messages(school_id);
CREATE INDEX idx_refresh_token    ON refresh_tokens(token) WHERE revoked = FALSE;
