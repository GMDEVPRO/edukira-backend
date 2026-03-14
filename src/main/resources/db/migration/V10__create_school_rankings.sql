CREATE TABLE school_rankings (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id        UUID NOT NULL REFERENCES schools(id),
    country_code     VARCHAR(3) NOT NULL,
    year             VARCHAR(10) NOT NULL,
    period           VARCHAR(20) NOT NULL,
    academic_score   NUMERIC(5,2),
    attendance_score NUMERIC(5,2),
    payment_score    NUMERIC(5,2),
    global_score     NUMERIC(5,2),
    national_rank    INT,
    regional_rank    INT,
    total_students   INT,
    pass_rate        NUMERIC(5,2),
    attendance_rate  NUMERIC(5,2),
    payment_rate     NUMERIC(5,2),
    computed_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_school_ranking UNIQUE (school_id, year, period)
);

CREATE INDEX idx_rankings_country ON school_rankings(country_code, year, global_score DESC);