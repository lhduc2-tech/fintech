package com.university.fintech.infrastructure.primary.restful.auth;

import com.university.fintech.infrastructure.primary.restful.request.auth.LoginRequest;
import com.university.fintech.infrastructure.primary.restful.request.auth.RegisterRequest;
import com.university.fintech.infrastructure.primary.restful.request.auth.VerifyOtpCodeRequest;
import com.university.fintech.infrastructure.primary.restful.response.auth.AuthResponse;
import com.university.fintech.infrastructure.primary.restful.response.auth.MessageResponse;
import com.university.fintech.domain.port.primary.AuthService;
import com.university.fintech.shared.constant.Constant;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

public class AuthOperator {

    @RestController
    @RequiredArgsConstructor
    @RequestMapping("${application.path.base.public}/auth")
    public static class PublicAuthOperator {

        private final AuthService authService;

        @PostMapping("/register")
        public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
            return authService.register(request);
        }

        @PostMapping("/login")
        public AuthResponse login(@Valid @RequestBody LoginRequest request) {
            return authService.login(request);
        }

        @PostMapping("/verify-otp-code")
        public MessageResponse getVerificationOTPCode(@Valid @RequestBody VerifyOtpCodeRequest request) {
            authService.getVerificationOTPCode(request);
            return new MessageResponse(Constant.MESSAGE_VERIFICATION_CODE_SENT_SUCCESSFULLY);
        }
    }

    @RestController
    @RequiredArgsConstructor
    @RequestMapping("${application.path.base.private}/auth")
    public static class PrivateAuthOperator {

        private final AuthService authService;

        @PostMapping("/logout")
        public MessageResponse logout(@RequestHeader("Authorization") String authorization) {
            authService.logout(extractBearerToken(authorization));
            return new MessageResponse("Logged out successfully.");
        }

        private String extractBearerToken(String authorization) {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return "";
            }
            return authorization.substring(7);
        }
    }
}
