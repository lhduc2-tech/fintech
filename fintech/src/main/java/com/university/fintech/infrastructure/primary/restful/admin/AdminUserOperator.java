package com.university.fintech.infrastructure.primary.restful.admin;

import com.university.fintech.domain.port.primary.AdminUserService;
import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminCreateUserRequest;
import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminResetPasswordRequest;
import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminResetPinRequest;
import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminUpdateUserRequest;
import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminUpdateUserStatusRequest;
import com.university.fintech.infrastructure.primary.restful.response.PageResponse;
import com.university.fintech.infrastructure.primary.restful.response.admin.user.AdminUserDetailResponse;
import com.university.fintech.infrastructure.primary.restful.response.admin.user.AdminUserSummaryResponse;
import com.university.fintech.infrastructure.primary.restful.response.auth.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminSearchUserRequest;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("${application.path.base.private}/admin/users")
public class AdminUserOperator {

    private final AdminUserService adminUserService;

    /**
     * Lấy danh sách người dùng phân trang kết hợp tìm kiếm và lọc qua POST request body.
     */
    @PostMapping("/search")
    public ResponseEntity<PageResponse<AdminUserSummaryResponse>> getUsers(
            @RequestBody(required = false) AdminSearchUserRequest request
    ) {
        PageResponse<AdminUserSummaryResponse> response = adminUserService.getUsers(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy chi tiết thông tin định danh, eKYC, phân quyền và ví của người dùng.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDetailResponse> getUserDetail(@PathVariable UUID id) {
        AdminUserDetailResponse response = adminUserService.getUserDetail(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Quản trị viên tạo mới người dùng (tự động kích hoạt ví và gán quyền).
     */
    @PostMapping(value = {"", "/create"})
    public ResponseEntity<AdminUserDetailResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        AdminUserDetailResponse response = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Cập nhật thông tin hồ sơ, hạn mức tier, điểm rủi ro, trạng thái eKYC hoặc quyền.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdminUserDetailResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody AdminUpdateUserRequest request
    ) {
        AdminUserDetailResponse response = adminUserService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Thay đổi trạng thái tài khoản người dùng (ACTIVE, LOCKED, SUSPENDED, INACTIVE).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<MessageResponse> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AdminUpdateUserStatusRequest request
    ) {
        MessageResponse response = adminUserService.updateUserStatus(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Mở khóa tài khoản bị khóa do đăng nhập/nhập PIN sai nhiều lần (reset các bộ đếm về 0).
     */
    @PostMapping("/{id}/unlock")
    public ResponseEntity<MessageResponse> unlockUser(@PathVariable UUID id) {
        MessageResponse response = adminUserService.unlockUser(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin đặt lại mật khẩu mới cho người dùng.
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @PathVariable UUID id,
            @Valid @RequestBody AdminResetPasswordRequest request
    ) {
        MessageResponse response = adminUserService.resetPassword(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin đặt lại mã PIN 6 số hoặc xóa mã PIN của người dùng.
     */
    @PostMapping("/{id}/reset-pin")
    public ResponseEntity<MessageResponse> resetPin(
            @PathVariable UUID id,
            @Valid @RequestBody AdminResetPinRequest request
    ) {
        MessageResponse response = adminUserService.resetPin(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa mềm người dùng (chuyển trạng thái sang INACTIVE để bảo toàn lịch sử giao dịch ví).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable UUID id) {
        MessageResponse response = adminUserService.deleteUser(id);
        return ResponseEntity.ok(response);
    }
}
