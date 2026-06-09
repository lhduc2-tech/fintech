package com.university.fintech.service.auth;

import com.university.fintech.domain.dto.RoleDTO;
import com.university.fintech.domain.dto.UserDTO;
import com.university.fintech.domain.dto.WalletDTO;
import com.university.fintech.domain.entity.RoleEntity;
import com.university.fintech.domain.entity.UserEntity;
import com.university.fintech.domain.mapper.UserMapper;
import com.university.fintech.domain.mapper.RoleMapper;
import com.university.fintech.domain.mapper.WalletMapper;
import com.university.fintech.exception.domain.AuthException;
import com.university.fintech.infrastructure.primary.restful.request.auth.LoginRequest;
import com.university.fintech.infrastructure.primary.restful.request.auth.RegisterRequest;
import com.university.fintech.infrastructure.primary.restful.request.auth.VerifyOtpCodeRequest;
import com.university.fintech.infrastructure.primary.restful.response.auth.AuthResponse;
import com.university.fintech.infrastructure.secondary.jpa.repository.RoleRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.UserRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletRepository;
import com.university.fintech.domain.port.primary.AuthMailService;
import com.university.fintech.domain.port.primary.AuthService;
import com.university.fintech.domain.port.primary.AccountUnlockCodeService;
import com.university.fintech.domain.port.primary.EmailVerificationTokenService;
import com.university.fintech.domain.port.primary.TokenBlacklistService;
import com.university.fintech.shared.constant.Constant;
import com.university.fintech.wire.security.Password;
import com.university.fintech.wire.security.impl.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WalletRepository walletRepository;
    private final Password password;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final WalletMapper walletMapper;
    private final JwtUtil jwtUtil;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final AccountUnlockCodeService accountUnlockCodeService;
    private final AuthMailService authMailService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String phoneNumber = request.getPhoneNumber().trim();
        String verificationCode = request.getVerificationCode().trim();
        validatePhoneNumber(phoneNumber);

        if (userRepository.existsByEmail(email)) {
            throw new AuthException(Constant.MESSAGE_EMAIL_ALREADY_EXISTS, Constant.ERROR_EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new AuthException(Constant.MESSAGE_PHONE_NUMBER_ALREADY_EXISTS, Constant.ERROR_PHONE_NUMBER_ALREADY_EXISTS);
        }
        if (!emailVerificationTokenService.isCodeValid(email, verificationCode)) {
            throw new AuthException(Constant.MESSAGE_INVALID_VERIFICATION_CODE, Constant.ERROR_INVALID_VERIFICATION_CODE);
        }

        RoleEntity userRole = roleRepository.findByCode(Constant.DEFAULT_ROLE_CODE)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_DEFAULT_ROLE_NOT_FOUND, Constant.ERROR_DEFAULT_ROLE_NOT_FOUND));
        RoleDTO userRoleDto = roleMapper.toDto(userRole);

        UserDTO userDto = UserDTO.builder()
                .email(email)
                .passwordHash(password.encode(request.getPassword()))
                .status(Constant.STATUS_ACTIVE)
                .fullName(normalizeFullName(request.getFullName()))
                .phoneNumber(phoneNumber)
                .failedLoginAttempts(0)
                .roleCodes(new HashSet<>(Set.of(userRoleDto.getCode())))
                .build();
        UserEntity user = userMapper.toEntity(userDto);
        user.setRoles(new HashSet<>(Set.of(userRole)));

        UserDTO savedUser = userMapper.toDto(userRepository.save(user));
        createWallet(savedUser);
        emailVerificationTokenService.consumeCode(email);

        return toResponse(savedUser, null);
    }

    @Override
    @Transactional(noRollbackFor = AuthException.class)
    public AuthResponse login(LoginRequest request) {
        String phoneNumber = request.getPhoneNumber().trim();
        validatePhoneNumber(phoneNumber);
        UserEntity userEntity = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_INVALID_PHONE_OR_PASSWORD, Constant.ERROR_INVALID_CREDENTIALS));
        UserDTO user = userMapper.toDto(userEntity);

        if (Constant.STATUS_LOCKED.equalsIgnoreCase(user.getStatus())) {
            unlockLockedAccount(request, user, userEntity);
        }
        if (!password.matches(request.getPassword(), user.getPasswordHash())) {
            int remainingLoginAttempts = handleFailedLogin(user, userEntity);
            throw new AuthException(
                    Constant.MESSAGE_INVALID_PHONE_OR_PASSWORD,
                    Constant.ERROR_INVALID_CREDENTIALS,
                    buildRemainingLoginAttemptsDetails(remainingLoginAttempts)
            );
        }
        if (!Constant.STATUS_ACTIVE.equalsIgnoreCase(user.getStatus())) {
            throw new AuthException(Constant.MESSAGE_EMAIL_NOT_VERIFIED, Constant.ERROR_EMAIL_NOT_VERIFIED);
        }

        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(OffsetDateTime.now());
        syncLoginState(user, userEntity);
        String accessToken = jwtUtil.generateToken(user);
        return toResponse(user, accessToken);
    }

    @Override
    @Transactional
    public void getVerificationOTPCode(VerifyOtpCodeRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new AuthException(Constant.MESSAGE_EMAIL_ALREADY_EXISTS, Constant.ERROR_EMAIL_ALREADY_EXISTS);
        }
        String verificationCode = emailVerificationTokenService.createCode(email);
        authMailService.sendVerificationCode(email, request.getFullName(), verificationCode);
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isBlank() || !jwtUtil.isTokenValid(token)) {
            throw new AuthException(Constant.MESSAGE_INVALID_TOKEN, Constant.ERROR_INVALID_TOKEN);
        }
        tokenBlacklistService.blacklist(token);
    }

    private AuthResponse toResponse(UserDTO user, String accessToken) {
        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .status(user.getStatus())
                .remainingLoginAttempts(resolveRemainingLoginAttempts(user))
                .roles(user.getRoleCodes())
                .accessToken(accessToken)
                .tokenType(accessToken == null ? null : Constant.TOKEN_TYPE_BEARER)
                .build();
    }

    private int resolveRemainingLoginAttempts(UserDTO user) {
        int failedLoginAttempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
        return Math.max(0, Constant.MAX_FAILED_LOGIN_ATTEMPTS - failedLoginAttempts);
    }

    private int handleFailedLogin(UserDTO user, UserEntity userEntity) {
        int failedLoginAttempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
        user.setFailedLoginAttempts(failedLoginAttempts + 1);
        syncLoginState(user, userEntity);

        if (user.getFailedLoginAttempts() >= Constant.MAX_FAILED_LOGIN_ATTEMPTS) {
            user.setStatus(Constant.STATUS_LOCKED);
            syncLoginState(user, userEntity);
            String unlockCode = accountUnlockCodeService.createCode(user.getPhoneNumber());
            authMailService.sendAccountUnlockCode(user.getEmail(), user.getFullName(), unlockCode);
            throw new AuthException(
                    Constant.MESSAGE_ACCOUNT_LOCKED_AFTER_FAILED_ATTEMPTS,
                    Constant.ERROR_ACCOUNT_LOCKED,
                    buildRemainingLoginAttemptsDetails(0)
            );
        }
        return resolveRemainingLoginAttempts(user);
    }

    private void unlockLockedAccount(LoginRequest request, UserDTO user, UserEntity userEntity) {
        if (request.getVerificationCode() == null || request.getVerificationCode().isBlank()) {
            throw new AuthException(
                    Constant.MESSAGE_ACCOUNT_LOCKED,
                    Constant.ERROR_ACCOUNT_LOCKED,
                    buildRemainingLoginAttemptsDetails(0)
            );
        }
        if (!password.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException(
                    Constant.MESSAGE_INVALID_PHONE_OR_PASSWORD,
                    Constant.ERROR_INVALID_CREDENTIALS,
                    buildRemainingLoginAttemptsDetails(0)
            );
        }
        if (!accountUnlockCodeService.isCodeValid(user.getPhoneNumber(), request.getVerificationCode().trim())) {
            throw new AuthException(Constant.MESSAGE_INVALID_UNLOCK_CODE, Constant.ERROR_INVALID_UNLOCK_CODE);
        }

        user.setStatus(Constant.STATUS_ACTIVE);
        user.setFailedLoginAttempts(0);
        syncLoginState(user, userEntity);
        accountUnlockCodeService.consumeCode(user.getPhoneNumber());
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (!phoneNumber.matches(Constant.PHONE_NUMBER_NUMERIC_PATTERN)) {
            throw new AuthException(Constant.MESSAGE_PHONE_NUMBER_NUMERIC_ONLY, Constant.ERROR_INVALID_PHONE_NUMBER);
        }
        if (!phoneNumber.matches(Constant.PHONE_NUMBER_PATTERN)) {
            throw new AuthException(Constant.MESSAGE_PHONE_NUMBER_EXACTLY_10_DIGITS, Constant.ERROR_INVALID_PHONE_NUMBER);
        }
    }

    private Map<String, Object> buildRemainingLoginAttemptsDetails(int remainingLoginAttempts) {
        return Map.of("remainingLoginAttempts", remainingLoginAttempts);
    }

    private void createWallet(UserDTO user) {
        if (walletRepository.existsByUserId(user.getId())) {
            return;
        }
        WalletDTO walletDto = WalletDTO.builder()
                .userId(user.getId())
                .status(Constant.STATUS_ACTIVE)
                .build();
        var wallet = walletMapper.toEntity(walletDto);
        wallet.setUser(userRepository.getReferenceById(user.getId()));
        walletRepository.save(wallet);
    }

    private void syncLoginState(UserDTO user, UserEntity userEntity) {
        userEntity.setStatus(user.getStatus());
        userEntity.setFailedLoginAttempts(user.getFailedLoginAttempts());
        userEntity.setLastLoginAt(user.getLastLoginAt());
    }

    private String normalizeFullName(String value) {
        String withoutSpecialVietnameseCharacters = value.trim()
                .replace('đ', 'd')
                .replace('Đ', 'D');
        String withoutDiacritics = Normalizer.normalize(withoutSpecialVietnameseCharacters, Normalizer.Form.NFD)
                .replaceAll(Constant.DIACRITIC_MARK_REGEX, Constant.EMPTY);
        return withoutDiacritics.toUpperCase(Locale.ROOT);
    }
}
