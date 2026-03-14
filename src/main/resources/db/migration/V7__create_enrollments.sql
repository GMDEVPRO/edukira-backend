CREATE TABLE enrollments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id           UUID NOT NULL REFERENCES schools(id),
    student_first_name  VARCHAR(100) NOT NULL,
    student_last_name   VARCHAR(100) NOT NULL,
    student_birth_date  VARCHAR(20),
    student_gender      VARCHAR(20),
    student_nationality VARCHAR(100),
    class_level         VARCHAR(50) NOT NULL,
    previous_school     VARCHAR(255),
    previous_average    NUMERIC(5,2),
    guardian_name       VARCHAR(200) NOT NULL,
    guardian_phone      VARCHAR(30) NOT NULL,
    guardian_whatsapp   VARCHAR(30),
    guardian_email      VARCHAR(255),
    guardian_profession VARCHAR(100),
    preferred_language  VARCHAR(5) DEFAULT 'fr',
    status              VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    payment_method      VARCHAR(20),
    payment_amount      NUMERIC(12,2),
    payment_confirmed   BOOLEAN NOT NULL DEFAULT FALSE,
    rejection_reason    TEXT,
    reviewed_by         UUID REFERENCES user_profiles(id),
    reviewed_at         TIMESTAMPTZ,
    student_id          UUID REFERENCES students(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_enrollments_school  ON enrollments(school_id);
CREATE INDEX idx_enrollments_status  ON enrollments(school_id, status);