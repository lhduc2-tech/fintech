package com.university.fintech.domain.port.primary;

import com.university.fintech.infrastructure.primary.restful.request.admin.fraudalert.AdminFraudAlertSearchRequest;
import com.university.fintech.infrastructure.primary.restful.request.admin.fraudalert.AdminUpdateFraudAlertStatusRequest;
import com.university.fintech.infrastructure.primary.restful.response.PageResponse;
import com.university.fintech.infrastructure.primary.restful.response.admin.fraudalert.AdminFraudAlertDetailResponse;
import com.university.fintech.infrastructure.primary.restful.response.admin.fraudalert.AdminFraudAlertSummaryResponse;
import com.university.fintech.infrastructure.primary.restful.response.auth.MessageResponse;

import java.util.UUID;

/**
 * Port primary cho quản lý Fraud Alerts từ phía Admin.
 */
public interface AdminFraudAlertService {

    /**
     * Tìm kiếm và phân trang danh sách fraud alerts với bộ lọc.
     */
    PageResponse<AdminFraudAlertSummaryResponse> searchAlerts(AdminFraudAlertSearchRequest request);

    /**
     * Lấy thông tin chi tiết một fraud alert theo ID.
     */
    AdminFraudAlertDetailResponse getAlertDetail(UUID id);

    /**
     * Cập nhật trạng thái xử lý của một fraud alert (REVIEW, RESOLVED, DISMISSED).
     */
    MessageResponse updateAlertStatus(UUID id, AdminUpdateFraudAlertStatusRequest request);
}
