-- =========================================================================
-- SCRIPT TẠO BẢNG FACEID_VERIFICATION_LOGS CHO HỆ THỐNG FINTECH & FACEID
-- Tương thích hoàn toàn với PostgreSQL và cấu trúc Entity/BaseAuditing của Fintech
-- =========================================================================

CREATE TABLE IF NOT EXISTS faceid_verification_logs (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    correlation_id          VARCHAR(100) NOT NULL,
    reference_code          VARCHAR(100),
    user_id                 UUID NOT NULL,
    amount                  NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    transaction_type        VARCHAR(50) DEFAULT 'TRANSFER',
    liveness_score          DOUBLE PRECISION,
    cosine_similarity_score DOUBLE PRECISION,
    verification_status     VARCHAR(32) NOT NULL, -- 'APPROVED', 'REJECTED', 'FAILED'
    rejection_reason        VARCHAR(500),
    created_at              TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at              TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    CONSTRAINT fk_faceid_verification_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Tạo Index để tối ưu truy vấn tra cứu theo mã giao dịch, người dùng và trạng thái xác thực
CREATE INDEX IF NOT EXISTS idx_faceid_logs_correlation_id ON faceid_verification_logs (correlation_id);
CREATE INDEX IF NOT EXISTS idx_faceid_logs_reference_code ON faceid_verification_logs (reference_code);
CREATE INDEX IF NOT EXISTS idx_faceid_logs_user_id ON faceid_verification_logs (user_id);
CREATE INDEX IF NOT EXISTS idx_faceid_logs_created_at ON faceid_verification_logs (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_faceid_logs_status ON faceid_verification_logs (verification_status);

-- Chú thích bảng và các cột
COMMENT ON TABLE faceid_verification_logs IS 'Nhật ký truy vết các phiên xác thực sinh trắc học FaceID (Trạm 1 Liveness + Trạm 2 Cosine Similarity)';
COMMENT ON COLUMN faceid_verification_logs.id IS 'Khóa chính UUID của bản ghi log';
COMMENT ON COLUMN faceid_verification_logs.correlation_id IS 'Mã UUID định danh luồng giao dịch xuyên suốt hệ thống Ví';
COMMENT ON COLUMN faceid_verification_logs.reference_code IS 'Mã tham chiếu giao dịch Fintech (nếu có)';
COMMENT ON COLUMN faceid_verification_logs.user_id IS 'Khóa ngoại liên kết tới bảng users (người thực hiện)';
COMMENT ON COLUMN faceid_verification_logs.amount IS 'Số tiền giao dịch cần duyệt sinh trắc học (>= 10,000,000 VNĐ)';
COMMENT ON COLUMN faceid_verification_logs.transaction_type IS 'Loại giao dịch: TRANSFER, WALLET_TO_BANK, CASH_IN, TOPUP, CASHOUT...';
COMMENT ON COLUMN faceid_verification_logs.liveness_score IS 'Điểm Anti-Spoofing Trạm 1 (MiniFASNet Softmax [0.0 - 1.0])';
COMMENT ON COLUMN faceid_verification_logs.cosine_similarity_score IS 'Điểm tương đồng sinh trắc học Trạm 2 (ArcFace 512D [0.0 - 1.0])';
COMMENT ON COLUMN faceid_verification_logs.verification_status IS 'Trạng thái xác thực: APPROVED (thành công), REJECTED (từ chối), FAILED (lỗi hệ thống)';
COMMENT ON COLUMN faceid_verification_logs.rejection_reason IS 'Lý do từ chối nếu không vượt qua xác thực';
COMMENT ON COLUMN faceid_verification_logs.created_at IS 'Thời điểm thực hiện xác thực FaceID';
