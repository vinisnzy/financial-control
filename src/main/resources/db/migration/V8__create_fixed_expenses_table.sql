CREATE
EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE fixed_expenses
(
    id                  UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    name                TEXT           NOT NULL,
    amount              DECIMAL(14, 2) NOT NULL,
    expense_category_id UUID           NOT NULL,
    payment_method_id   UUID           NOT NULL,
    due_day             SMALLINT       NOT NULL,
    start_date          DATE           NOT NULL,
    end_date            DATE,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT (now()),
    updated_at          TIMESTAMPTZ,
    user_id             UUID           NOT NULL,

    CONSTRAINT fk_fixed_expenses_expense_category FOREIGN KEY (expense_category_id) REFERENCES expense_categories (id),
    CONSTRAINT fk_fixed_expenses_payment_method FOREIGN KEY (payment_method_id) REFERENCES payment_methods (id),
    CONSTRAINT fk_fixed_expenses_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_fixed_expenses_expense_category_id on fixed_expenses (expense_category_id);
CREATE INDEX idx_fixed_expenses_payment_method_id on fixed_expenses (payment_method_id);
CREATE INDEX idx_fixed_expenses_user_id on fixed_expenses (user_id);