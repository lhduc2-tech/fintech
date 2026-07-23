-- Migration: Tạo bảng payment_transactions trong DB fintech
-- Bảng này được CoreService dùng để:
--   1. Lưu audit trail của mọi giao dịch đã xử lý
--   2. Idempotency guard: tránh xử lý trùng khi PaymentGateway retry

CREATE TABLE IF NOT EXISTS payment_transactions
(
    id               UUID        NOT NULL PRIMARY KEY,
    correlation_id   VARCHAR     NOT NULL UNIQUE,
    reference_code   VARCHAR     NOT NULL,
    transaction_type VARCHAR     NOT NULL,
    amount           NUMERIC     NOT NULL,
    status           VARCHAR     NOT NULL,
    failure_reason   VARCHAR,
    idempotency_key  VARCHAR     NOT NULL UNIQUE,
    processed_at     TIMESTAMP WITH TIME ZONE,
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    created_by       VARCHAR,
    updated_at       TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_by       VARCHAR
);

COMMENT ON TABLE payment_transactions IS 'Audit trail của CoreService - mọi giao dịch đã được xử lý';
COMMENT ON COLUMN payment_transactions.correlation_id   IS 'ID để track request từ fintech qua PaymentGateway';
COMMENT ON COLUMN payment_transactions.reference_code   IS 'referenceCode từ wallet_transactions của fintech';
COMMENT ON COLUMN payment_transactions.transaction_type IS 'BANK_TO_WALLET / WALLET_TO_BANK / WALLET_TRANSFER_OUT';
COMMENT ON COLUMN payment_transactions.status           IS 'SUCCESS / FAILED';
COMMENT ON COLUMN payment_transactions.idempotency_key  IS 'Khóa chống xử lý trùng giao dịch';
COMMENT ON COLUMN payment_transactions.processed_at     IS 'Thời điểm CoreService hoàn thành xử lý';

CREATE INDEX IF NOT EXISTS idx_payment_tx_reference_code
    ON payment_transactions (reference_code);

CREATE INDEX IF NOT EXISTS idx_payment_tx_processed_at
    ON payment_transactions (processed_at DESC);
