package com.university.fintech.domain.port.primary;

import com.university.fintech.infrastructure.primary.restful.request.auth.LoginRequest;
import com.university.fintech.infrastructure.primary.restful.request.auth.RegisterRequest;
import com.university.fintech.infrastructure.primary.restful.request.auth.VerifyOtpCodeRequest;
import com.university.fintech.infrastructure.primary.restful.response.auth.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void getVerificationOTPCode(VerifyOtpCodeRequest request);

    void logout(String token);
}
