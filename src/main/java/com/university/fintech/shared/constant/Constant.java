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
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

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
    public static final String WALLET_TRANSACTION_TEMPLATE = "classpath:template/wallet-transaction-email.html";
    public static final String WALLET_TRANSACTION_ERROR_TEMPLATE = "classpath:template/wallet-transaction-error-email.html";
    public static final String EMAIL_VERIFICATION_SUBJECT = "Mã OTP xác thực đăng ký Fintech";
    public static final String ACCOUNT_UNLOCK_SUBJECT = "Mã mở khóa tài khoản Fintech";
    public static final String PASSWORD_RESET_SUBJECT = "Mã OTP lấy lại mật khẩu Fintech";
    public static final String WALLET_TRANSACTION_SUBJECT = "Thông báo giao dịch ví Fintech";
    public static final String WALLET_TRANSACTION_ERROR_SUBJECT = "Giao dịch ví Fintech không thành công";
    public static final String EMAIL_TEMPLATE_FULL_NAME_PLACEHOLDER = "{{fullName}}";
    public static final String EMAIL_TEMPLATE_VERIFICATION_CODE_PLACEHOLDER = "{{verificationCode}}";
    public static final String EMAIL_TEMPLATE_UNLOCK_CODE_PLACEHOLDER = "{{unlockCode}}";
    public static final String EMAIL_TEMPLATE_RESET_CODE_PLACEHOLDER = "{{resetCode}}";
    public static final String EMAIL_TEMPLATE_TRANSACTION_TYPE_PLACEHOLDER = "{{transactionType}}";
    public static final String EMAIL_TEMPLATE_AMOUNT_PLACEHOLDER = "{{amount}}";
    public static final String EMAIL_TEMPLATE_STATUS_PLACEHOLDER = "{{status}}";
    public static final String EMAIL_TEMPLATE_STATUS_COLOR_PLACEHOLDER = "{{statusColor}}";
    public static final String EMAIL_TEMPLATE_STATUS_BACKGROUND_PLACEHOLDER = "{{statusBackground}}";
    public static final String EMAIL_TEMPLATE_REFERENCE_CODE_PLACEHOLDER = "{{referenceCode}}";
    public static final String EMAIL_TEMPLATE_DESCRIPTION_PLACEHOLDER = "{{description}}";
    public static final String EMAIL_TEMPLATE_ERROR_MESSAGE_PLACEHOLDER = "{{errorMessage}}";
    public static final String EMAIL_TEMPLATE_RECIPIENT_SECTION_PLACEHOLDER = "{{recipientSection}}";
    public static final String EMAIL_FALLBACK_DISPLAY_NAME = "bạn";

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
    public static final String ERROR_WALLET_NOT_FOUND = "WALLET_NOT_FOUND";
    public static final String ERROR_BANK_NOT_FOUND = "BANK_NOT_FOUND";
    public static final String ERROR_BANK_ACCOUNT_NOT_FOUND = "BANK_ACCOUNT_NOT_FOUND";
    public static final String ERROR_BANK_ACCOUNT_ALREADY_LINKED = "BANK_ACCOUNT_ALREADY_LINKED";
    public static final String ERROR_INSUFFICIENT_WALLET_BALANCE = "INSUFFICIENT_WALLET_BALANCE";
    public static final String ERROR_INSUFFICIENT_BANK_BALANCE = "INSUFFICIENT_BANK_BALANCE";
    public static final String ERROR_DUPLICATED_TRANSACTION = "DUPLICATED_TRANSACTION";
    public static final String ERROR_IDEMPOTENCY_PAYLOAD_MISMATCH = "IDEMPOTENCY_PAYLOAD_MISMATCH";
    public static final String ERROR_TRANSACTION_NOT_FOUND = "TRANSACTION_NOT_FOUND";
    public static final String ERROR_RECIPIENT_NOT_FOUND = "RECIPIENT_NOT_FOUND";
    public static final String ERROR_SELF_WALLET_TRANSFER = "SELF_WALLET_TRANSFER";
    public static final String ERROR_USER_NOT_ACTIVE = "USER_NOT_ACTIVE";
    public static final String ERROR_WALLET_NOT_ACTIVE = "WALLET_NOT_ACTIVE";
    public static final String ERROR_BANK_NOT_ACTIVE = "BANK_NOT_ACTIVE";
    public static final String ERROR_BANK_ACCOUNT_NOT_ACTIVE = "BANK_ACCOUNT_NOT_ACTIVE";
    public static final String ERROR_WALLET_TRANSACTION_AMOUNT_LIMIT_EXCEEDED = "WALLET_TRANSACTION_AMOUNT_LIMIT_EXCEEDED";

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
    public static final String MESSAGE_WALLET_NOT_FOUND = "Wallet was not found.";
    public static final String MESSAGE_BANK_NOT_FOUND = "Bank was not found.";
    public static final String MESSAGE_BANK_ACCOUNT_NOT_FOUND = "Linked bank account was not found.";
    public static final String MESSAGE_BANK_ACCOUNT_ALREADY_LINKED = "Bank is already linked to this wallet.";
    public static final String MESSAGE_INSUFFICIENT_WALLET_BALANCE = "Wallet balance is insufficient.";
    public static final String MESSAGE_INSUFFICIENT_BANK_BALANCE = "Bank account balance is insufficient.";
    public static final String MESSAGE_DUPLICATED_TRANSACTION = "Transaction idempotency key already exists.";
    public static final String MESSAGE_IDEMPOTENCY_PAYLOAD_MISMATCH = "Transaction idempotency key was already used with different transfer details.";
    public static final String MESSAGE_TRANSACTION_NOT_FOUND = "Transaction was not found.";
    public static final String MESSAGE_RECIPIENT_NOT_FOUND = "Recipient was not found.";
    public static final String MESSAGE_SELF_WALLET_TRANSFER = "Cannot transfer money to your own wallet.";
    public static final String MESSAGE_USER_NOT_ACTIVE = "User is not active.";
    public static final String MESSAGE_WALLET_NOT_ACTIVE = "Wallet is not active.";
    public static final String MESSAGE_BANK_NOT_ACTIVE = "Bank is not active.";
    public static final String MESSAGE_BANK_ACCOUNT_NOT_ACTIVE = "Linked bank account is not active.";
    public static final String MESSAGE_WALLET_TRANSACTION_AMOUNT_POSITIVE = "Amount must be greater than 0.";
    public static final String MESSAGE_WALLET_TRANSACTION_AMOUNT_INTEGER = "Amount must be an integer.";
    public static final String MESSAGE_WALLET_TRANSACTION_AMOUNT_MAX_PER_TRANSACTION = "Amount must not exceed 50,000,000 per transaction.";
    public static final String MESSAGE_WALLET_TRANSACTION_DAILY_LIMIT_EXCEEDED = "Daily wallet transaction limit exceeded.";
    public static final String MESSAGE_WALLET_TRANSACTION_MONTHLY_LIMIT_EXCEEDED = "Monthly wallet transaction limit exceeded.";
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
    public static final String WALLET_TRANSACTION_TYPE_BANK_TO_WALLET = "BANK_TO_WALLET";
    public static final String WALLET_TRANSACTION_TYPE_WALLET_TO_BANK = "WALLET_TO_BANK";
    public static final String WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT = "WALLET_TRANSFER_OUT";
    public static final String WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_IN = "WALLET_TRANSFER_IN";
    public static final String WALLET_TRANSFER_CREDIT_IDEMPOTENCY_SUFFIX = ":CREDIT";
    public static final String LEDGER_ENTRY_TYPE_CREDIT = "CREDIT";
    public static final String LEDGER_ENTRY_TYPE_DEBIT = "DEBIT";
    public static final String WALLET_PROCESSING_TEST_PENDING_TOKEN = "PENDING";
    public static final String WALLET_PROCESSING_TEST_FAILED_TOKEN = "FAILED";
    public static final String WALLET_TRANSACTION_MAX_AMOUNT_PER_TRANSACTION = "50000000";
    public static final String WALLET_TRANSACTION_DAILY_LIMIT = "100000000";
    public static final String WALLET_TRANSACTION_MONTHLY_LIMIT = "500000000";
}
