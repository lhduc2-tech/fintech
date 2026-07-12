-- Script seed dữ liệu bank_accounts cho PaymentGateway DB
-- Chạy script này để copy dữ liệu từ bảng banks của fintech sang bank_accounts của paymentgateway
-- Thay thế các giá trị balance cho phù hợp với môi trường của bạn

-- Cấu trúc bảng bank_accounts (đã tạo trên Supabase):
-- CREATE TABLE bank_accounts (
--     id UUID PRIMARY KEY,
--     bank_code VARCHAR(20) NOT NULL,
--     account_number VARCHAR(50) NOT NULL,
--     balance NUMERIC(19,2) NOT NULL DEFAULT 0,
--     status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
--     created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
--     updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
--     UNIQUE(bank_code, account_number)
-- );

-- Bạn cần INSERT các bank account giống với dữ liệu trong bảng banks của fintech DB
-- Ví dụ (thay giá trị bank_code, account_number theo dữ liệu thực của bạn):

INSERT INTO bank_accounts (id, bank_code, account_number, balance, status)
SELECT
    gen_random_uuid(),
    bank_code,
    account_number,
    COALESCE(balance, 1000000000), -- Mặc định 1 tỷ nếu chưa có balance
    status
FROM (
    VALUES
        -- Thay thế bằng dữ liệu thực từ bảng banks của fintech DB
        -- Truy vấn: SELECT bank_code, account_number, balance, status FROM banks;
        ('VCB', '1234567890', 5000000000, 'ACTIVE'),
        ('TCB', '0987654321', 5000000000, 'ACTIVE')
) AS t(bank_code, account_number, balance, status)
ON CONFLICT (bank_code, account_number) DO NOTHING;

-- Kiểm tra kết quả:
-- SELECT * FROM bank_accounts;
