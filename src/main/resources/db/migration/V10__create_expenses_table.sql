CREATE
EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE expenses
(
    id                     UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    name                   TEXT           NOT NULL,
    amount                 DECIMAL(14, 2) NOT NULL,
    expense_category_id    UUID           NOT NULL,
    payment_method_id      UUID           NOT NULL,
    due_date               DATE           NOT NULL,
    paid_at                DATE,
    fixed_expense_id       UUID,
    installment_expense_id UUID,
    installment_number     SMALLINT,

    created_at             TIMESTAMPTZ    NOT NULL DEFAULT (now()),
    updated_at             TIMESTAMPTZ,
    user_id                UUID           NOT NULL,

    CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_expenses_expense_category FOREIGN KEY (expense_category_id) REFERENCES expense_categories (id),
    CONSTRAINT fk_expenses_payment_method FOREIGN KEY (payment_method_id) REFERENCES payment_methods (id),
    CONSTRAINT fk_expenses_fixed_expense FOREIGN KEY (fixed_expense_id) REFERENCES fixed_expenses (id) ON DELETE SET NULL,
    CONSTRAINT fk_expenses_installment_expense FOREIGN KEY (installment_expense_id) REFERENCES installment_expenses (id)
);

CREATE INDEX idx_expenses_user_due_date on expenses (user_id, due_date);
CREATE INDEX idx_expenses_expense_category_id on expenses (expense_category_id);
CREATE INDEX idx_expenses_payment_method_id on expenses (payment_method_id);
CREATE INDEX idx_expenses_fixed_expense_id on expenses (fixed_expense_id);
CREATE INDEX idx_expenses_installment_expense_id on expenses (installment_expense_id);
