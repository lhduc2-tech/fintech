package com.university.fintech.infrastructure.primary.restful.request.admin.fraudalert;

import com.university.fintech.infrastructure.primary.restful.request.BaseRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

/**
 * Request DTO để tìm kiếm và phân trang fraud alerts.
 * Kế thừa BaseRequest để sử dụng toPageable() chuẩn hóa phân trang.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AdminFraudAlertSearchRequest extends BaseRequest {

    /** Từ khóa tìm kiếm theo referenceCode hoặc userEmail hoặc userFullName */
    private String search;

    /** Lọc theo mức rủi ro: LOW | MEDIUM | HIGH */
    private String riskLevel;

    /** Lọc theo trạng thái: NEW | REVIEW | RESOLVED | DISMISSED */
    private String status;

    /** Lọc từ ngày */
    private OffsetDateTime fromCreatedAt;

    /** Lọc đến ngày */
    private OffsetDateTime toCreatedAt;
}
