package com.university.fintech.domain.port.primary;

import com.university.fintech.domain.dto.FraudDetectionPayload;

import java.util.concurrent.CompletableFuture;

/**
 * Port giao tiếp với Fraud Detection AI Microservice.
 * Thực hiện sàng lọc rủi ro giao dịch bất đồng bộ (Non-blocking).
 */
public interface FraudDetectionService {

    /**
     * Gửi giao dịch sang Fraud Detection Service một cách bất đồng bộ (Asynchronous / Non-blocking).
     * Hoàn toàn không ảnh hưởng tới luồng giao dịch chính nếu Fraud Service bị chậm hoặc gặp sự cố.
     *
     * @param payload Thông tin chi tiết giao dịch cùng các đặc trưng rủi ro
     */
    void screenTransactionAsync(FraudDetectionPayload payload);
}
