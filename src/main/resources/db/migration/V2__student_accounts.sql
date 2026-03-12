-- ══════════════════════════════════════════════════════════════════
-- Edukira · V2 — Contas de alunos e documentos
-- ══════════════════════════════════════════════════════════════════

-- STUDENT_ACCOUNTS (criadas pelo próprio aluno ou tutor)
CREATE TABLE student_accounts (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id       UUID REFERENCES students(id),          -- null até admin vincular
    school_id        UUID NOT NULL REFERENCES schools(id),
    full_name        VARCHAR(255) NOT NULL,
    document_number  VARCHAR(50) NOT NULL,
    document_type    VARCHAR(20),                           -- BI, PASSPORT, CEDULA
    email            VARCHAR(255),
    phone            VARCHAR(30),
    password_hash    TEXT NOT NULL,
    preferred_language VARCHAR(5) NOT NULL DEFAULT 'fr',
    status           VARCHAR(25) NOT NULL DEFAULT 'PENDING_APPROVAL',
    rejection_reason TEXT,
    approved_by      UUID,                                  -- userId do admin
    approved_at      TIMESTAMPTZ,
    last_login       TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (document_number, school_id),
    UNIQUE (email)
);

-- STUDENT_DOCUMENTS (boletins, certidões, declarações visíveis ao aluno)
CREATE TABLE student_documents (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id  UUID NOT NULL REFERENCES students(id),
    school_id   UUID NOT NULL REFERENCES schools(id),
    type        VARCHAR(40) NOT NULL,
    title       VARCHAR(255) NOT NULL,
    file_url    TEXT NOT NULL,
    file_size   BIGINT,
    school_year VARCHAR(10),
    visible     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índices
CREATE INDEX idx_student_accounts_school   ON student_accounts(school_id, status);
CREATE INDEX idx_student_accounts_doc      ON student_accounts(document_number, school_id);
CREATE INDEX idx_student_accounts_email    ON student_accounts(email);
CREATE INDEX idx_student_docs_student      ON student_documents(student_id, visible);
