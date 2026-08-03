package com.university.fintech.infrastructure.primary.restful.report;

import com.university.fintech.domain.dto.SystemReportDTO;
import com.university.fintech.domain.port.primary.SystemReportService;
import com.university.fintech.infrastructure.primary.restful.request.report.SystemReportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("${application.path.base.private}/reports/system")
public class ReportOperator {

    private final SystemReportService systemReportService;

    /**
     * Lấy dữ liệu thống kê tổng quan và chỉ số độ ổn định hệ thống dưới dạng JSON.
     */
    @PostMapping("/metrics")
    public ResponseEntity<SystemReportDTO> getSystemMetrics(
            @RequestBody(required = false) SystemReportRequest request
    ) {
        OffsetDateTime from = request != null ? request.getFrom() : null;
        OffsetDateTime to = request != null ? request.getTo() : null;

        SystemReportDTO metrics = systemReportService.getSystemStabilityMetrics(from, to);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Xuất file báo cáo Excel (.xlsx) tổng hợp 4 Sheet phân tích hệ thống:
     * 1. Tổng quan & Đánh giá Độ ổn định (Card + KPIs)
     * 2. Thống kê Chi tiết Người dùng
     * 3. Thống kê Chi tiết Giao dịch & Luồng tiền
     * 4. Danh sách Chi tiết Giao dịch Lỗi & Sự cố
     */
    @PostMapping("/excel")
    public ResponseEntity<byte[]> exportSystemReportExcel(
            @RequestBody(required = false) SystemReportRequest request
    ) {
        OffsetDateTime from = request != null ? request.getFrom() : null;
        OffsetDateTime to = request != null ? request.getTo() : null;

        byte[] excelBytes = systemReportService.generateSystemStabilityExcelReport(from, to);

        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "Bao_Cao_Do_On_Dinh_He_Thong_" + timestamp + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
