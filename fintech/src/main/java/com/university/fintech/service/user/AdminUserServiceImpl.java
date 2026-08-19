package com.university.fintech.service.user;

import com.university.fintech.domain.entity.PermissionEntity;
import com.university.fintech.domain.entity.RoleEntity;
import com.university.fintech.domain.entity.UserEntity;
import com.university.fintech.domain.entity.WalletEntity;
import com.university.fintech.domain.mapper.UserMapper;
import com.university.fintech.domain.port.primary.AdminUserService;
import com.university.fintech.exception.domain.AuthException;
import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminCreateUserRequest;
import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminResetPasswordRequest;
import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminResetPinRequest;
import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminUpdateUserRequest;
import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminUpdateUserStatusRequest;
import com.university.fintech.infrastructure.primary.restful.response.PageResponse;
import com.university.fintech.infrastructure.primary.restful.response.admin.user.AdminUserDetailResponse;
import com.university.fintech.infrastructure.primary.restful.response.admin.user.AdminUserSummaryResponse;
import com.university.fintech.infrastructure.primary.restful.response.auth.MessageResponse;
import com.university.fintech.infrastructure.secondary.jpa.repository.RoleRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.UserRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletRepository;
import com.university.fintech.infrastructure.secondary.jpa.specification.UserSpecification;
import com.university.fintech.shared.constant.Constant;
import com.university.fintech.wire.security.Password;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.university.fintech.infrastructure.primary.restful.request.admin.user.AdminSearchUserRequest;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WalletRepository walletRepository;
    private final Password password;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserSummaryResponse> getUsers(AdminSearchUserRequest request) {
        AdminSearchUserRequest safeRequest = request == null ? new AdminSearchUserRequest() : request;
        Specification<UserEntity> spec = UserSpecification.filterUsers(
                safeRequest.getSearch(),
                safeRequest.getStatus(),
                safeRequest.getTier(),
                safeRequest.getEkycStatus(),
                safeRequest.getRoleCode(),
                safeRequest.getFromCreatedAt(),
                safeRequest.getToCreatedAt()
        );

        Page<UserEntity> page = userRepository.findAll(spec, safeRequest.toPageable());
        return PageResponse.of(page, this::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserDetail(UUID id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_USER_NOT_FOUND, Constant.ERROR_USER_NOT_FOUND));

        Optional<WalletEntity> walletOpt = walletRepository.findByUserId(id);
        return toDetailResponse(userEntity, walletOpt.orElse(null));
    }

    @Override
    @Transactional
    public AdminUserDetailResponse createUser(AdminCreateUserRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String phoneNumber = request.getPhoneNumber().trim();
        validatePhoneNumber(phoneNumber);

        if (userRepository.existsByEmail(email)) {
            throw new AuthException(Constant.MESSAGE_EMAIL_ALREADY_EXISTS, Constant.ERROR_EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new AuthException(Constant.MESSAGE_PHONE_NUMBER_ALREADY_EXISTS, Constant.ERROR_PHONE_NUMBER_ALREADY_EXISTS);
        }
        if (request.getIdentityNumber() != null && !request.getIdentityNumber().isBlank()) {
            String identity = request.getIdentityNumber().trim();
            if (userRepository.existsByIdentityNumber(identity)) {
                throw new AuthException(Constant.MESSAGE_IDENTITY_NUMBER_ALREADY_EXISTS, Constant.ERROR_IDENTITY_NUMBER_ALREADY_EXISTS);
            }
        }

        Set<RoleEntity> roles = resolveRoles(request.getRoleCodes());

        int calculatedTier = 0;
        if (request.getTier() != null) {
            calculatedTier = request.getTier();
        } else if (request.getDob() != null) {
            int age = Period.between(request.getDob(), LocalDate.now()).getYears();
            calculatedTier = age >= 18 ? 1 : 0;
        }

        String initialStatus = (request.getStatus() != null && !request.getStatus().isBlank())
                ? request.getStatus().trim().toUpperCase()
                : Constant.STATUS_ACTIVE;

        UserEntity userEntity = UserEntity.builder()
                .email(email)
                .passwordHash(password.encode(request.getPassword()))
                .status(initialStatus)
                .fullName(normalizeFullName(request.getFullName()))
                .phoneNumber(phoneNumber)
                .identityNumber(request.getIdentityNumber() != null ? request.getIdentityNumber().trim() : null)
                .dob(request.getDob())
                .hometown(request.getHometown() != null ? request.getHometown().trim() : null)
                .tier(calculatedTier)
                .riskScore(request.getRiskScore())
                .failedLoginAttempts(0)
                .pinFailedAttempts(0)
                .roles(roles)
                .build();

        UserEntity savedUser = userRepository.save(userEntity);
        createWalletIfNotExist(savedUser);

        Optional<WalletEntity> walletOpt = walletRepository.findByUserId(savedUser.getId());
        return toDetailResponse(savedUser, walletOpt.orElse(null));
    }

    @Override
    @Transactional
    public AdminUserDetailResponse updateUser(UUID id, AdminUpdateUserRequest request) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_USER_NOT_FOUND, Constant.ERROR_USER_NOT_FOUND));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            userEntity.setFullName(normalizeFullName(request.getFullName()));
        }
        if (request.getIdentityNumber() != null) {
            String newIdentity = request.getIdentityNumber().trim();
            if (!newIdentity.isBlank() && !newIdentity.equalsIgnoreCase(userEntity.getIdentityNumber())) {
                if (userRepository.existsByIdentityNumberAndIdNot(newIdentity, id)) {
                    throw new AuthException(Constant.MESSAGE_IDENTITY_NUMBER_ALREADY_EXISTS, Constant.ERROR_IDENTITY_NUMBER_ALREADY_EXISTS);
                }
                userEntity.setIdentityNumber(newIdentity);
            } else if (newIdentity.isBlank()) {
                userEntity.setIdentityNumber(null);
            }
        }
        if (request.getDob() != null) {
            userEntity.setDob(request.getDob());
        }
        if (request.getHometown() != null) {
            userEntity.setHometown(request.getHometown().trim());
        }
        if (request.getTier() != null) {
            userEntity.setTier(request.getTier());
        }
        if (request.getRiskScore() != null) {
            userEntity.setRiskScore(request.getRiskScore());
        }
        if (request.getEkycStatus() != null && !request.getEkycStatus().isBlank()) {
            userEntity.setEkycStatus(request.getEkycStatus().trim().toUpperCase());
            if (Constant.EKYC_STATUS_VERIFIED.equalsIgnoreCase(userEntity.getEkycStatus()) && userEntity.getEkycRegisteredAt() == null) {
                userEntity.setEkycRegisteredAt(OffsetDateTime.now());
            }
        }
        if (request.getRoleCodes() != null && !request.getRoleCodes().isEmpty()) {
            Set<RoleEntity> roles = resolveRoles(request.getRoleCodes());
            userEntity.setRoles(roles);
        }

        UserEntity updatedUser = userRepository.save(userEntity);
        Optional<WalletEntity> walletOpt = walletRepository.findByUserId(updatedUser.getId());
        return toDetailResponse(updatedUser, walletOpt.orElse(null));
    }

    @Override
    @Transactional
    public MessageResponse updateUserStatus(UUID id, AdminUpdateUserStatusRequest request) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_USER_NOT_FOUND, Constant.ERROR_USER_NOT_FOUND));

        String newStatus = request.getStatus().trim().toUpperCase();
        userEntity.setStatus(newStatus);
        userRepository.save(userEntity);

        return new MessageResponse(Constant.MESSAGE_USER_STATUS_UPDATED_SUCCESSFULLY);
    }

    @Override
    @Transactional
    public MessageResponse unlockUser(UUID id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_USER_NOT_FOUND, Constant.ERROR_USER_NOT_FOUND));

        userEntity.setFailedLoginAttempts(0);
        userEntity.setPinFailedAttempts(0);
        if (Constant.STATUS_LOCKED.equalsIgnoreCase(userEntity.getStatus())) {
            userEntity.setStatus(Constant.STATUS_ACTIVE);
        }
        userRepository.save(userEntity);

        return new MessageResponse(Constant.MESSAGE_USER_UNLOCKED_SUCCESSFULLY);
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(UUID id, AdminResetPasswordRequest request) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_USER_NOT_FOUND, Constant.ERROR_USER_NOT_FOUND));

        userEntity.setPasswordHash(password.encode(request.getNewPassword().trim()));
        userEntity.setFailedLoginAttempts(0);
        userRepository.save(userEntity);

        return new MessageResponse(Constant.MESSAGE_USER_PASSWORD_RESET_SUCCESSFULLY);
    }

    @Override
    @Transactional
    public MessageResponse resetPin(UUID id, AdminResetPinRequest request) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_USER_NOT_FOUND, Constant.ERROR_USER_NOT_FOUND));

        if (request.getNewPin() != null && !request.getNewPin().isBlank()) {
            userEntity.setPinHash(password.encode(request.getNewPin().trim()));
        } else {
            userEntity.setPinHash(null);
        }
        userEntity.setPinFailedAttempts(0);
        userRepository.save(userEntity);

        return new MessageResponse(Constant.MESSAGE_USER_PIN_RESET_SUCCESSFULLY);
    }

    @Override
    @Transactional
    public MessageResponse deleteUser(UUID id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_USER_NOT_FOUND, Constant.ERROR_USER_NOT_FOUND));

        userEntity.setStatus(Constant.STATUS_INACTIVE);
        userRepository.save(userEntity);

        return new MessageResponse(Constant.MESSAGE_USER_DELETED_SUCCESSFULLY);
    }

    private Set<RoleEntity> resolveRoles(Set<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            RoleEntity defaultRole = roleRepository.findByCode(Constant.DEFAULT_ROLE_CODE)
                    .orElseThrow(() -> new AuthException(Constant.MESSAGE_DEFAULT_ROLE_NOT_FOUND, Constant.ERROR_DEFAULT_ROLE_NOT_FOUND));
            return new HashSet<>(Set.of(defaultRole));
        }

        List<RoleEntity> foundRoles = roleRepository.findByCodeIn(roleCodes);
        if (foundRoles.isEmpty()) {
            throw new AuthException(Constant.MESSAGE_ROLE_NOT_FOUND, Constant.ERROR_ROLE_NOT_FOUND);
        }
        return new HashSet<>(foundRoles);
    }

    private void createWalletIfNotExist(UserEntity user) {
        if (walletRepository.existsByUserId(user.getId())) {
            return;
        }
        WalletEntity wallet = WalletEntity.builder()
                .user(user)
                .availableBalance(BigDecimal.ZERO)
                .holdBalance(BigDecimal.ZERO)
                .status(Constant.STATUS_ACTIVE)
                .limitation(1)
                .build();
        walletRepository.save(wallet);
    }

    private AdminUserSummaryResponse toSummaryResponse(UserEntity entity) {
        Set<String> roleCodes = entity.getRoles() != null
                ? entity.getRoles().stream().map(RoleEntity::getCode).collect(Collectors.toSet())
                : Collections.emptySet();

        return AdminUserSummaryResponse.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .fullName(entity.getFullName())
                .identityNumber(entity.getIdentityNumber())
                .status(entity.getStatus())
                .tier(entity.getTier())
                .riskScore(entity.getRiskScore())
                .ekycStatus(entity.getEkycStatus())
                .failedLoginAttempts(entity.getFailedLoginAttempts())
                .pinFailedAttempts(entity.getPinFailedAttempts())
                .roleCodes(roleCodes)
                .createdAt(entity.getCreatedAt())
                .lastLoginAt(entity.getLastLoginAt())
                .build();
    }

    private AdminUserDetailResponse toDetailResponse(UserEntity entity, WalletEntity wallet) {
        Set<String> roleCodes = entity.getRoles() != null
                ? entity.getRoles().stream().map(RoleEntity::getCode).collect(Collectors.toSet())
                : Collections.emptySet();

        Set<String> permissionCodes = entity.getRoles() != null
                ? entity.getRoles().stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .map(PermissionEntity::getCode)
                .collect(Collectors.toSet())
                : Collections.emptySet();

        AdminUserDetailResponse.WalletInfo walletInfo = null;
        if (wallet != null) {
            BigDecimal available = wallet.getAvailableBalance() != null ? wallet.getAvailableBalance() : BigDecimal.ZERO;
            BigDecimal hold = wallet.getHoldBalance() != null ? wallet.getHoldBalance() : BigDecimal.ZERO;
            BigDecimal total = available.add(hold);

            walletInfo = AdminUserDetailResponse.WalletInfo.builder()
                    .walletId(wallet.getId())
                    .balance(total)
                    .availableBalance(available)
                    .holdingBalance(hold)
                    .walletStatus(wallet.getStatus())
                    .limitation(wallet.getLimitation())
                    .build();
        }

        return AdminUserDetailResponse.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .fullName(entity.getFullName())
                .identityNumber(entity.getIdentityNumber())
                .dob(entity.getDob())
                .hometown(entity.getHometown())
                .status(entity.getStatus())
                .tier(entity.getTier())
                .riskScore(entity.getRiskScore())
                .failedLoginAttempts(entity.getFailedLoginAttempts())
                .pinFailedAttempts(entity.getPinFailedAttempts())
                .hasPin(entity.getPinHash() != null && !entity.getPinHash().isBlank())
                .ekycStatus(entity.getEkycStatus())
                .ekycRegisteredAt(entity.getEkycRegisteredAt())
                .roleCodes(roleCodes)
                .permissionCodes(permissionCodes)
                .wallet(walletInfo)
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .lastLoginAt(entity.getLastLoginAt())
                .build();
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (!phoneNumber.matches(Constant.PHONE_NUMBER_NUMERIC_PATTERN)) {
            throw new AuthException(Constant.MESSAGE_PHONE_NUMBER_NUMERIC_ONLY, Constant.ERROR_INVALID_PHONE_NUMBER);
        }
        if (!phoneNumber.matches(Constant.PHONE_NUMBER_PATTERN)) {
            throw new AuthException(Constant.MESSAGE_PHONE_NUMBER_EXACTLY_10_DIGITS, Constant.ERROR_INVALID_PHONE_NUMBER);
        }
    }

    private String normalizeFullName(String value) {
        if (value == null) {
            return null;
        }
        String withoutSpecialVietnameseCharacters = value.trim()
                .replace('đ', 'd')
                .replace('Đ', 'D');
        String withoutDiacritics = Normalizer.normalize(withoutSpecialVietnameseCharacters, Normalizer.Form.NFD)
                .replaceAll(Constant.DIACRITIC_MARK_REGEX, Constant.EMPTY);
        return withoutDiacritics.toUpperCase(Locale.ROOT);
    }
}
