package com.university.fintech.domain.port.primary;

import com.university.fintech.domain.dto.SystemReportDTO;

import java.time.OffsetDateTime;

public interface SystemReportService {

    /**
     * Lấy các chỉ số thống kê tình trạng hoạt động và độ ổn định của hệ thống.
     *
     * @param from Khoảng thời gian bắt đầu (tùy chọn)
     * @param to Khoảng thời gian kết thúc (tùy chọn)
     * @return DTO chứa thông tin tổng hợp
     */
    SystemReportDTO getSystemStabilityMetrics(OffsetDateTime from, OffsetDateTime to);

    /**
     * Xuất báo cáo Excel (.xlsx) gồm 4 Sheet chi tiết đánh giá hệ thống.
     *
     * @param from Khoảng thời gian bắt đầu (tùy chọn)
     * @param to Khoảng thời gian kết thúc (tùy chọn)
     * @return Mảng byte chứa dữ liệu file Excel .xlsx
     */
    byte[] generateSystemStabilityExcelReport(OffsetDateTime from, OffsetDateTime to);
}
