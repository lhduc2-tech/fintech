-- Thêm cột pin_hash và pin_failed_attempts vào bảng users
-- Chạy migration này trên database trước khi deploy

ALTER TABLE users ADD COLUMN IF NOT EXISTS pin_hash VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS pin_failed_attempts INTEGER DEFAULT 0;
