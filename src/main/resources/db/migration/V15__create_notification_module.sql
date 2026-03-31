-- ══════════════════════════════════════════════════════
-- Edukira — V15 · Notification Module
-- Log centralizado de todas as notificações enviadas
-- ══════════════════════════════════════════════════════

CREATE TABLE notification_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id       UUID REFERENCES schools(id),
    type            VARCHAR(50)  NOT NULL,   -- ATTENDANCE_ABSENCE, GRADE_PUBLISHED, etc.
    channel         VARCHAR(20)  NOT NULL,   -- SMS, WHATSAPP, EMAIL, PUSH
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',  -- PENDING, SENT, FAILED, RETRYING
    recipient_phone VARCHAR(30),
    recipient_name  VARCHAR(200),
    entity_id       UUID,                    -- student_id, seller_id, etc.
    entity_type     VARCHAR(50),             -- STUDENT, TUTOR, SELLER, USER
    body            TEXT NOT NULL,
    external_id     TEXT,                    -- ID retornado por Africa's Talking / Twilio
    error_message   TEXT,
    retry_count     INT NOT NULL DEFAULT 0,
    sent_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notif_school        ON notification_logs(school_id);
CREATE INDEX idx_notif_type          ON notification_logs(school_id, type);
CREATE INDEX idx_notif_status        ON notification_logs(status);
CREATE INDEX idx_notif_entity        ON notification_logs(entity_id, entity_type);
CREATE INDEX idx_notif_created       ON notification_logs(created_at DESC);
