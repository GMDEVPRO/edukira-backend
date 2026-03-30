-- ══════════════════════════════════════════════════════
-- Edukira — V12 · Leads da Landing Page
-- ══════════════════════════════════════════════════════

CREATE TABLE leads (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(200) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    school      VARCHAR(255) NOT NULL,
    phone       VARCHAR(50),
    message     TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'NEW',
    source      VARCHAR(50) NOT NULL DEFAULT 'LANDING_PAGE',
    language    VARCHAR(5)  NOT NULL DEFAULT 'fr',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_leads_status    ON leads(status);
CREATE INDEX idx_leads_email     ON leads(email);
CREATE INDEX idx_leads_created   ON leads(created_at DESC);