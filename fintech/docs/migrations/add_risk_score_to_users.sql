-- Thêm cột risk_score vào bảng users
-- Chạy migration này trên database trước khi deploy

ALTER TABLE users ADD COLUMN IF NOT EXISTS risk_score NUMERIC(5, 2) DEFAULT 0.00;
COMMENT ON COLUMN users.risk_score IS 'Điểm đánh giá rủi ro người dùng (0.00 - 100.00)';
