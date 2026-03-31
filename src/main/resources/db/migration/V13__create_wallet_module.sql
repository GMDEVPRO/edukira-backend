-- ══════════════════════════════════════════════════════
-- Edukira — V13 · Wallet Module
-- Carteira digital dos vendedores + histórico de transacções
-- ══════════════════════════════════════════════════════

CREATE TABLE wallets (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id           UUID UNIQUE REFERENCES marketplace_sellers(id),
    available_balance   NUMERIC(14,2) NOT NULL DEFAULT 0,
    pending_balance     NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_earned        NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_withdrawn     NUMERIC(14,2) NOT NULL DEFAULT 0,
    currency            CHAR(5)  NOT NULL DEFAULT 'XOF',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE wallet_transactions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id       UUID NOT NULL REFERENCES wallets(id),
    type            VARCHAR(20) NOT NULL,   -- CREDIT, DEBIT, WITHDRAWAL, COMMISSION, REFUND
    amount          NUMERIC(14,2) NOT NULL,
    balance_after   NUMERIC(14,2) NOT NULL,
    currency        CHAR(5)  NOT NULL DEFAULT 'XOF',
    reference_id    UUID,                   -- order_id, withdrawal_id, etc.
    reference_type  VARCHAR(50),            -- ORDER, WITHDRAWAL, COMMISSION
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_wallet_seller      ON wallets(seller_id);
CREATE INDEX idx_wallet_tx_wallet   ON wallet_transactions(wallet_id);
CREATE INDEX idx_wallet_tx_type     ON wallet_transactions(wallet_id, type);
CREATE INDEX idx_wallet_tx_ref      ON wallet_transactions(reference_id);
