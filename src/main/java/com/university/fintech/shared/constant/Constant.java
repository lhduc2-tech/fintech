package com.university.fintech.shared.constant;

import lombok.experimental.UtilityClass;

@SuppressWarnings("all")
@UtilityClass
public class Constant {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_PENDING_EMAIL_VERIFICATION = "PENDING_EMAIL_VERIFICATION";
    public static final String STATUS_LOCKED = "LOCKED";

    public static final String DEFAULT_ROLE_CODE = "USER";

    public static final String TOKEN_TYPE_BEARER = "Bearer";

    public static final String SPRING_MAIL_USERNAME_PROPERTY = "${spring.mail.username:}";
    public static final String EMAIL_VERIFICATION_TTL_SECONDS_PROPERTY = "${application.auth.email-verification-ttl-seconds}";
    public static final String EMAIL_VERIFICATION_SEND_MAX_ATTEMPTS_PROPERTY = "${application.auth.email-verification-send-max-attempts}";
    public static final String EMAIL_VERIFICATION_SEND_WINDOW_SECONDS_PROPERTY = "${application.auth.email-verification-send-window-seconds}";
    public static final String EMAIL_VERIFICATION_SEND_COOLDOWN_SECONDS_PROPERTY = "${application.auth.email-verification-send-cooldown-seconds}";
    public static final String ACCOUNT_UNLOCK_CODE_TTL_SECONDS_PROPERTY = "${application.auth.account-unlock-code-ttl-seconds}";
    public static final String PASSWORD_RESET_CODE_TTL_SECONDS_PROPERTY = "${application.auth.password-reset-code-ttl-seconds}";

    public static final String CHARSET_UTF_8 = "UTF-8";

    public static final String EMAIL_VERIFICATION_TEMPLATE = "classpath:template/verification-email.html";
    public static final String ACCOUNT_UNLOCK_TEMPLATE = "classpath:template/account-unlock-email.html";
    public static final String PASSWORD_RESET_TEMPLATE = "classpath:template/password-reset-email.html";
    public static final String EMAIL_VERIFICATION_SUBJECT = "Ma OTP xac thuc dang ky Fintech";
    public static final String ACCOUNT_UNLOCK_SUBJECT = "Ma mo khoa tai khoan Fintech";
    public static final String PASSWORD_RESET_SUBJECT = "Ma OTP lay lai mat khau Fintech";
    public static final String EMAIL_TEMPLATE_FULL_NAME_PLACEHOLDER = "{{fullName}}";
    public static final String EMAIL_TEMPLATE_VERIFICATION_CODE_PLACEHOLDER = "{{verificationCode}}";
    public static final String EMAIL_TEMPLATE_UNLOCK_CODE_PLACEHOLDER = "{{unlockCode}}";
    public static final String EMAIL_TEMPLATE_RESET_CODE_PLACEHOLDER = "{{resetCode}}";
    public static final String EMAIL_FALLBACK_DISPLAY_NAME = "ban";

    public static final String EMAIL_VERIFY_REDIS_PREFIX = "auth:email-verify:";
    public static final String EMAIL_VERIFY_SEND_COUNT_REDIS_PREFIX = "auth:email-verify:send-count:";
    public static final String EMAIL_VERIFY_SEND_COOLDOWN_REDIS_PREFIX = "auth:email-verify:send-cooldown:";
    public static final String ACCOUNT_UNLOCK_REDIS_PREFIX = "auth:account-unlock:";
    public static final String PASSWORD_RESET_REDIS_PREFIX = "auth:password-reset:";
    public static final String TOKEN_BLACKLIST_REDIS_PREFIX = "auth:blacklist:";
    public static final String TOKEN_BLACKLIST_VALUE = "1";
    public static final int VERIFICATION_CODE_ORIGIN = 100000;
    public static final int VERIFICATION_CODE_BOUND = 900000;
    public static final int ACCOUNT_UNLOCK_CODE_ORIGIN = 100000;
    public static final int ACCOUNT_UNLOCK_CODE_BOUND = 900000;
    public static final int PASSWORD_RESET_CODE_ORIGIN = 100000;
    public static final int PASSWORD_RESET_CODE_BOUND = 900000;
    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;

    public static final String DIACRITIC_MARK_REGEX = "\\p{M}";
    public static final String EMPTY = "";

    public static final String HTML_AMPERSAND = "&";
    public static final String HTML_AMPERSAND_ESCAPED = "&amp;";
    public static final String HTML_LESS_THAN = "<";
    public static final String HTML_LESS_THAN_ESCAPED = "&lt;";
    public static final String HTML_GREATER_THAN = ">";
    public static final String HTML_GREATER_THAN_ESCAPED = "&gt;";
    public static final String HTML_QUOTATION_MARK = "\"";
    public static final String HTML_QUOTATION_MARK_ESCAPED = "&quot;";

    public static final String ERROR_EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";
    public static final String ERROR_PHONE_NUMBER_ALREADY_EXISTS = "PHONE_NUMBER_ALREADY_EXISTS";
    public static final String ERROR_INVALID_PHONE_NUMBER = "INVALID_PHONE_NUMBER";
    public static final String ERROR_DEFAULT_ROLE_NOT_FOUND = "DEFAULT_ROLE_NOT_FOUND";
    public static final String ERROR_INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String ERROR_EMAIL_NOT_VERIFIED = "EMAIL_NOT_VERIFIED";
    public static final String ERROR_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
    public static final String ERROR_INVALID_VERIFICATION_CODE = "INVALID_VERIFICATION_CODE";
    public static final String ERROR_INVALID_UNLOCK_CODE = "INVALID_UNLOCK_CODE";
    public static final String ERROR_INVALID_PASSWORD_RESET_CODE = "INVALID_PASSWORD_RESET_CODE";
    public static final String ERROR_USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String ERROR_INVALID_TOKEN = "INVALID_TOKEN";
    public static final String ERROR_VERIFICATION_CODE_RATE_LIMITED = "VERIFICATION_CODE_RATE_LIMITED";

    public static final String MESSAGE_EMAIL_ALREADY_EXISTS = "Email already exists.";
    public static final String MESSAGE_PHONE_NUMBER_ALREADY_EXISTS = "Phone number already exists.";
    public static final String MESSAGE_DEFAULT_ROLE_NOT_FOUND = "Default USER role does not exist.";
    public static final String MESSAGE_INVALID_PHONE_OR_PASSWORD = "Invalid phone number or password.";
    public static final String MESSAGE_EMAIL_NOT_VERIFIED = "Please verify your email before logging in.";
    public static final String MESSAGE_ACCOUNT_LOCKED = "Account is locked. Please unlock it with your password and the verification code sent to your email.";
    public static final String MESSAGE_ACCOUNT_LOCKED_AFTER_FAILED_ATTEMPTS = "Account is locked because password was entered incorrectly too many times. A verification code has been sent to your email.";
    public static final String MESSAGE_INVALID_VERIFICATION_CODE = "Email verification code is invalid or expired.";
    public static final String MESSAGE_INVALID_UNLOCK_CODE = "Account unlock code is invalid or expired.";
    public static final String MESSAGE_INVALID_PASSWORD_RESET_CODE = "Password reset code is invalid or expired.";
    public static final String MESSAGE_USER_NOT_FOUND = "User was not found.";
    public static final String MESSAGE_INVALID_TOKEN = "Token is invalid.";
    public static final String MESSAGE_VERIFICATION_CODE_SEND_COOLDOWN = "Please wait before requesting another verification code.";
    public static final String MESSAGE_VERIFICATION_CODE_SEND_LIMIT_EXCEEDED = "Verification code request limit exceeded. Please try again later.";
    public static final String MESSAGE_BUILD_VERIFICATION_EMAIL_FAILED = "Could not build verification email.";
    public static final String MESSAGE_LOAD_VERIFICATION_EMAIL_TEMPLATE_FAILED = "Could not load verification email template.";
    public static final String PHONE_NUMBER_NUMERIC_PATTERN = "^\\d+$";
    public static final String PHONE_NUMBER_PATTERN = "^\\d{10}$";
    public static final String MESSAGE_PHONE_NUMBER_EXACTLY_10_DIGITS = "Phone number must contain exactly 10 digits.";
    public static final String MESSAGE_PHONE_NUMBER_NUMERIC_ONLY = "Phone number must contain numeric characters only.";
    public static final String MESSAGE_ACCOUNT_UNLOCKED_SUCCESSFULLY = "Account unlocked successfully.";
    public static final String MESSAGE_VERIFICATION_CODE_SENT_SUCCESSFULLY = "Verification code sent successfully.";
    public static final String MESSAGE_PASSWORD_RESET_CODE_SENT_SUCCESSFULLY = "Password reset code sent successfully.";
    public static final String MESSAGE_PASSWORD_RESET_SUCCESSFULLY = "Password reset successfully.";
}
