package com.university.fintech.infrastructure.primary.restful.admin;

import com.university.fintech.domain.port.primary.AdminFraudAlertService;
import com.university.fintech.infrastructure.primary.restful.request.admin.fraudalert.AdminFraudAlertSearchRequest;
import com.university.fintech.infrastructure.primary.restful.request.admin.fraudalert.AdminUpdateFraudAlertStatusRequest;
import com.university.fintech.infrastructure.primary.restful.response.PageResponse;
import com.university.fintech.infrastructure.primary.restful.response.admin.fraudalert.AdminFraudAlertDetailResponse;
import com.university.fintech.infrastructure.primary.restful.response.admin.fraudalert.AdminFraudAlertSummaryResponse;
import com.university.fintech.infrastructure.primary.restful.response.auth.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller quản lý Fraud Alerts dành cho Admin.
 * Base path: ${application.path.base.private}/admin/fraud-alerts
 */
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("${application.path.base.private}/admin/fraud-alerts")
public class AdminFraudAlertOperator {

    private final AdminFraudAlertService adminFraudAlertService;

    /**
     * Tìm kiếm danh sách fraud alerts theo bộ lọc (risk level, status, keyword, date range).
     */
    @PostMapping("/search")
    public ResponseEntity<PageResponse<AdminFraudAlertSummaryResponse>> searchAlerts(
            @RequestBody(required = false) AdminFraudAlertSearchRequest request
    ) {
        PageResponse<AdminFraudAlertSummaryResponse> response = adminFraudAlertService.searchAlerts(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy chi tiết đầy đủ của một fraud alert (bao gồm triggered rules, threat warnings, payload snapshot).
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminFraudAlertDetailResponse> getAlertDetail(@PathVariable UUID id) {
        AdminFraudAlertDetailResponse response = adminFraudAlertService.getAlertDetail(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật trạng thái xử lý của fraud alert: REVIEW | RESOLVED | DISMISSED.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<MessageResponse> updateAlertStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AdminUpdateFraudAlertStatusRequest request
    ) {
        MessageResponse response = adminFraudAlertService.updateAlertStatus(id, request);
        return ResponseEntity.ok(response);
    }
}
