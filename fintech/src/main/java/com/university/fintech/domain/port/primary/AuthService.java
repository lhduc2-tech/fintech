package com.university.fintech.domain.port.primary;

import com.university.fintech.infrastructure.primary.restful.request.auth.LoginRequest;
import com.university.fintech.infrastructure.primary.restful.request.auth.ForgotPasswordRequest;
import com.university.fintech.infrastructure.primary.restful.request.auth.RegisterRequest;
import com.university.fintech.infrastructure.primary.restful.request.auth.ResetPasswordRequest;
import com.university.fintech.infrastructure.primary.restful.request.auth.VerifyOtpCodeRequest;
import com.university.fintech.infrastructure.primary.restful.response.auth.AuthResponse;
import com.university.fintech.infrastructure.primary.restful.response.auth.UserProfileResponse;
import java.util.UUID;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void getVerificationOTPCode(VerifyOtpCodeRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void logout(String token);

    UserProfileResponse getUserProfile(UUID userId);
}
