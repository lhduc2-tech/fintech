package com.university.fintech.domain.port.primary;

import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminCreateUserRequest;
import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminResetPasswordRequest;
import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminResetPinRequest;
import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminSearchUserRequest;
import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminUpdateUserRequest;
import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminUpdateUserStatusRequest;
import com.university.fintech.infrastructure.primary.restful.response.PageResponse;
import com.university.fintech.infrastructure.primary.restful.response.admin.user.AdminUserDetailResponse;
import com.university.fintech.infrastructure.primary.restful.response.admin.user.AdminUserSummaryResponse;
import com.university.fintech.infrastructure.primary.restful.response.auth.MessageResponse;

import java.util.UUID;

public interface AdminUserService {

    PageResponse<AdminUserSummaryResponse> getUsers(AdminSearchUserRequest request);

    AdminUserDetailResponse getUserDetail(UUID id);

    AdminUserDetailResponse createUser(AdminCreateUserRequest request);

    AdminUserDetailResponse updateUser(UUID id, AdminUpdateUserRequest request);

    MessageResponse updateUserStatus(UUID id, AdminUpdateUserStatusRequest request);

    MessageResponse unlockUser(UUID id);

    MessageResponse resetPassword(UUID id, AdminResetPasswordRequest request);

    MessageResponse resetPin(UUID id, AdminResetPinRequest request);

    MessageResponse deleteUser(UUID id);
}
