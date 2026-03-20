-- ─── 1. SELLERS ───
CREATE TABLE marketplace_sellers (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_profile_id     UUID NOT NULL REFERENCES user_profiles(id),
    school_id           UUID REFERENCES schools(id),
    display_name        VARCHAR(120) NOT NULL,
    bio                 TEXT,
    seller_type         VARCHAR(30) NOT NULL, -- TEACHER=20%, SCHOOL=15%, PUBLISHER=15%, EDUKIRA_PARTNER=13%
    commission_rate     NUMERIC(5,2) NOT NULL DEFAULT 20.00,
    wallet_balance      NUMERIC(14,2) NOT NULL DEFAULT 0.00,
    mobile_money_number VARCHAR(30),
    mobile_money_operator VARCHAR(20),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    approved_at         TIMESTAMPTZ
);

-- ─── 2. PRODUCTS ───
CREATE TABLE marketplace_products (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id       UUID NOT NULL REFERENCES marketplace_sellers(id),
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    category        VARCHAR(40) NOT NULL,
    subject         VARCHAR(80),
    level           VARCHAR(80),
    language        VARCHAR(10) NOT NULL DEFAULT 'fr',
    price           NUMERIC(14,2) NOT NULL,
    currency        VARCHAR(5) NOT NULL DEFAULT 'XOF',
    file_url        TEXT,
    preview_url     TEXT,
    thumbnail_url   TEXT,
    pages           INTEGER,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    downloads       INTEGER NOT NULL DEFAULT 0,
    rating_sum      NUMERIC(6,2) NOT NULL DEFAULT 0,
    rating_count    INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    approved_at     TIMESTAMPTZ
);

-- ─── 3. ORDERS ───
CREATE TABLE marketplace_orders (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id      UUID NOT NULL REFERENCES marketplace_products(id),
    buyer_school_id UUID REFERENCES schools(id),
    buyer_user_id   UUID REFERENCES user_profiles(id),
    amount_paid     NUMERIC(14,2) NOT NULL,
    currency        VARCHAR(5) NOT NULL,
    commission_rate NUMERIC(5,2) NOT NULL,
    commission_amt  NUMERIC(14,2) NOT NULL,
    seller_amount   NUMERIC(14,2) NOT NULL,
    payment_method  VARCHAR(30),
    payment_ref     VARCHAR(100),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    download_url    TEXT,
    purchased_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── 4. WITHDRAWALS ───
CREATE TABLE marketplace_withdrawals (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id           UUID NOT NULL REFERENCES marketplace_sellers(id),
    amount              NUMERIC(14,2) NOT NULL,
    withdrawal_method   VARCHAR(20) NOT NULL,
    mobile_operator     VARCHAR(20),
    mobile_number       VARCHAR(30),
    bank_name           VARCHAR(100),
    bank_account_name   VARCHAR(150),
    bank_account_number VARCHAR(50),
    bank_iban           VARCHAR(50),
    bank_swift          VARCHAR(20),
    bank_country        VARCHAR(3),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reference           VARCHAR(100),
    notes               TEXT,
    requested_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at        TIMESTAMPTZ
);

-- ─── INDEXES ───
CREATE INDEX idx_mp_sellers_user      ON marketplace_sellers(user_profile_id);
CREATE INDEX idx_mp_sellers_status    ON marketplace_sellers(status);
CREATE INDEX idx_mp_products_seller   ON marketplace_products(seller_id);
CREATE INDEX idx_mp_products_status   ON marketplace_products(status);
CREATE INDEX idx_mp_products_category ON marketplace_products(category);
CREATE INDEX idx_mp_orders_product    ON marketplace_orders(product_id);
CREATE INDEX idx_mp_orders_buyer      ON marketplace_orders(buyer_school_id);
CREATE INDEX idx_mp_withdrawals_seller ON marketplace_withdrawals(seller_id);
CREATE INDEX idx_mp_withdrawals_status ON marketplace_withdrawals(status);