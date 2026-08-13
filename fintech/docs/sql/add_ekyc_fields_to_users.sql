-- Migration: Thêm cột eKYC vào bảng users
-- Áp dụng cho tích hợp FaceID / eKYC theo chuẩn QĐ 2345/QĐ-NHNN
-- Chạy thủ công trên database trước khi deploy version này.

-- Trạng thái eKYC: NULL = chưa đăng ký | 'PENDING' = đang xử lý | 'VERIFIED' = đã xác minh
ALTER TABLE users ADD COLUMN IF NOT EXISTS ekyc_status VARCHAR(20);

-- Thời điểm hoàn tất đăng ký eKYC sinh trắc học thành công
ALTER TABLE users ADD COLUMN IF NOT EXISTS ekyc_registered_at TIMESTAMPTZ;

-- Index để query nhanh danh sách user theo ekyc_status (admin report)
CREATE INDEX IF NOT EXISTS idx_users_ekyc_status ON users (ekyc_status);

-- Comment cho DBA
COMMENT ON COLUMN users.ekyc_status IS 'Trạng thái eKYC sinh trắc học: NULL=chưa đăng ký, PENDING=đang xử lý, VERIFIED=đã xác minh';
COMMENT ON COLUMN users.ekyc_registered_at IS 'Thời điểm hoàn tất đăng ký eKYC thành công (FaceID Python server xác nhận)';
