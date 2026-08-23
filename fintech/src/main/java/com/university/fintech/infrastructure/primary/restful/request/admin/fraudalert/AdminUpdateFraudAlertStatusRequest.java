package com.university.fintech.infrastructure.primary.restful.request.admin.fraudalert;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO để cập nhật trạng thái xử lý của một fraud alert.
 */
@Getter
@Setter
public class AdminUpdateFraudAlertStatusRequest {

    @NotBlank(message = "Status không được để trống")
    @Pattern(regexp = "REVIEW|RESOLVED|DISMISSED", message = "Status phải là REVIEW, RESOLVED hoặc DISMISSED")
    private String status;

    /** Ghi chú của admin khi xử lý (tuỳ chọn) */
    private String note;
}
