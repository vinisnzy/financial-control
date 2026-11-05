CREATE
EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE incomes
(
    id                 UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    name               TEXT           NOT NULL,
    amount             DECIMAL(14, 2) NOT NULL,
    income_category_id UUID           NOT NULL,
    bank_account_id    UUID           NOT NULL,
    expected_at        DATE           NOT NULL,
    received_at        DATE,
    fixed_income_id    UUID,

    created_at         TIMESTAMPTZ    NOT NULL DEFAULT (now()),
    updated_at         TIMESTAMPTZ,
    user_id            UUID           NOT NULL,

    CONSTRAINT uq_fixed_income_month UNIQUE (fixed_income_id, expected_at),

    CONSTRAINT fk_incomes_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_incomes_category FOREIGN KEY (income_category_id) REFERENCES income_categories (id),
    CONSTRAINT fk_incomes_bank_account FOREIGN KEY (bank_account_id) REFERENCES bank_accounts (id),
    CONSTRAINT fk_incomes_fixed_income FOREIGN KEY (fixed_income_id) REFERENCES fixed_incomes (id) ON DELETE SET NULL
);

CREATE INDEX idx_incomes_user_expected on incomes (user_id, expected_at);
CREATE INDEX idx_incomes_income_category_id ON incomes (income_category_id);
CREATE INDEX idx_incomes_bank_account_id ON incomes (bank_account_id);
CREATE INDEX idx_incomes_fixed_income_id ON incomes (fixed_income_id);

