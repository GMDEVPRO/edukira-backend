-- ══════════════════════════════════════════════════════
-- Edukira — V14 · Commission Module
-- Registo de comissões Edukira por venda no marketplace
-- ══════════════════════════════════════════════════════

CREATE TABLE commissions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id            UUID NOT NULL REFERENCES marketplace_orders(id),
    seller_id           UUID NOT NULL REFERENCES marketplace_sellers(id),
    sale_amount         NUMERIC(14,2) NOT NULL,
    commission_rate     NUMERIC(5,2)  NOT NULL,
    commission_amount   NUMERIC(14,2) NOT NULL,
    seller_net          NUMERIC(14,2) NOT NULL,
    currency            CHAR(5) NOT NULL DEFAULT 'XOF',
    product_title       VARCHAR(255),
    seller_display_name VARCHAR(255),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_commission_seller  ON commissions(seller_id);
CREATE INDEX idx_commission_order   ON commissions(order_id);
CREATE INDEX idx_commission_date    ON commissions(created_at);
