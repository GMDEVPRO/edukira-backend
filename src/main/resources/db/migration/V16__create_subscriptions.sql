-- ══════════════════════════════════════════════════════
-- Edukira — V16 · Subscription Module
-- Planos Starter / Pro / Enterprise por escola
-- ══════════════════════════════════════════════════════

CREATE TABLE subscriptions (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id               UUID NOT NULL UNIQUE REFERENCES schools(id),
    plan                    VARCHAR(20)  NOT NULL DEFAULT 'STARTER',
    status                  VARCHAR(20)  NOT NULL DEFAULT 'TRIAL',
    student_limit           INT          NOT NULL DEFAULT 200,
    trial_ends_at           TIMESTAMPTZ,
    current_period_start    TIMESTAMPTZ,
    current_period_end      TIMESTAMPTZ,
    external_ref            TEXT,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sub_school  ON subscriptions(school_id);
CREATE INDEX idx_sub_status  ON subscriptions(status);
CREATE INDEX idx_sub_trial   ON subscriptions(trial_ends_at) WHERE status = 'TRIAL';

-- Comentários dos limites por plano
COMMENT ON COLUMN subscriptions.student_limit IS
    'Starter=200, Pro=1000, Enterprise=-1 (ilimitado)';
COMMENT ON COLUMN subscriptions.status IS
    'TRIAL | ACTIVE | EXPIRED | CANCELLED';