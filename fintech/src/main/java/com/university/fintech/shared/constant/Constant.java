package com.university.fintech.shared.constant;

import lombok.experimental.UtilityClass;

@SuppressWarnings("all")
@UtilityClass
public class Constant {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_RISK = "RISK";
    public static final String STATUS_UNLINKED = "UNLINKED";
    public static final String STATUS_PENDING_EMAIL_VERIFICATION = "PENDING_EMAIL_VERIFICATION";
    public static final String STATUS_LOCKED = "LOCKED";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_TIMED_OUT = "TIMED_OUT";

    public static final String DEFAULT_ROLE_CODE = "USER";

    public static final String TOKEN_TYPE_BEARER = "Bearer";

    public static final String SPRING_MAIL_USERNAME_PROPERTY = "${spring.mail.username:}";
    public static final String EMAIL_VERIFICATION_TTL_SECONDS_PROPERTY = "${application.auth.email-verification-ttl-seconds}";
    public static final String EMAIL_VERIFICATION_SEND_MAX_ATTEMPTS_PROPERTY = "${application.auth.email-verification-send-max-attempts}";
    public static final String EMAIL_VERIFICATION_SEND_WINDOW_SECONDS_PROPERTY = "${application.auth.email-verification-send-window-seconds}";
    public static final String EMAIL_VERIFICATION_SEND_COOLDOWN_SECONDS_PROPERTY = "${application.auth.email-verification-send-cooldown-seconds}";
    public static final String ACCOUNT_UNLOCK_CODE_TTL_SECONDS_PROPERTY = "${application.auth.account-unlock-code-ttl-seconds}";
    public static final String PASSWORD_RESET_CODE_TTL_SECONDS_PROPERTY = "${application.auth.password-reset-code-ttl-seconds}";
    public static final String WALLET_PIN_OTP_TTL_SECONDS_PROPERTY = "${application.wallet.pin-otp-ttl-seconds:300}";
    public static final String WALLET_TX_OTP_TTL_SECONDS_PROPERTY = "${application.wallet.tx-otp-ttl-seconds:300}";
    public static final String WALLET_PIN_MAX_FAILED_ATTEMPTS_PROPERTY = "${application.wallet.pin-max-failed-attempts:5}";
    public static final String WALLET_PIN_PENDING_TTL_SECONDS_PROPERTY = "${application.wallet.pin-pending-ttl-seconds:300}";

    public static final String CHARSET_UTF_8 = "UTF-8";

    public static final String EMAIL_VERIFICATION_TEMPLATE = "classpath:template/verification-email.html";
    public static final String ACCOUNT_UNLOCK_TEMPLATE = "classpath:template/account-unlock-email.html";
    public static final String PASSWORD_RESET_TEMPLATE = "classpath:template/password-reset-email.html";
    public static final String PIN_OTP_TEMPLATE = "classpath:template/pin-otp-email.html";
    public static final String TRANSACTION_OTP_TEMPLATE = "classpath:template/transaction-otp-email.html";
    public static final String WALLET_TRANSACTION_TEMPLATE = "classpath:template/wallet-transaction-email.html";
    public static final String WALLET_TRANSACTION_ERROR_TEMPLATE = "classpath:template/wallet-transaction-error-email.html";
    public static final String EMAIL_VERIFICATION_SUBJECT = "Mã OTP xác thực đăng ký Fintech";
    public static final String ACCOUNT_UNLOCK_SUBJECT = "Mã mở khóa tài khoản Fintech";
    public static final String PASSWORD_RESET_SUBJECT = "Mã OTP lấy lại mật khẩu Fintech";
    public static final String PIN_OTP_SUBJECT = "Mã OTP xác nhận tạo mã PIN Fintech";
    public static final String TRANSACTION_OTP_SUBJECT = "Mã OTP xác thực giao dịch Fintech";
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
    public static final String EMAIL_TEMPLATE_PIN_OTP_PLACEHOLDER = "{{pinOtpCode}}";
    public static final String EMAIL_TEMPLATE_TRANSACTION_OTP_PLACEHOLDER = "{{transactionOtpCode}}";
    public static final String EMAIL_FALLBACK_DISPLAY_NAME = "bạn";

    public static final String EMAIL_VERIFY_REDIS_PREFIX = "auth:email-verify:";
    public static final String EMAIL_VERIFY_SEND_COUNT_REDIS_PREFIX = "auth:email-verify:send-count:";
    public static final String EMAIL_VERIFY_SEND_COOLDOWN_REDIS_PREFIX = "auth:email-verify:send-cooldown:";
    public static final String ACCOUNT_UNLOCK_REDIS_PREFIX = "auth:account-unlock:";
    public static final String PASSWORD_RESET_REDIS_PREFIX = "auth:password-reset:";
    public static final String TOKEN_BLACKLIST_REDIS_PREFIX = "auth:blacklist:";
    public static final String TOKEN_BLACKLIST_VALUE = "1";
    public static final String WALLET_PIN_OTP_REDIS_PREFIX = "wallet:pin-otp:";
    public static final String WALLET_PIN_PENDING_REDIS_PREFIX = "wallet:pin-pending:";
    public static final String WALLET_TX_OTP_REDIS_PREFIX = "wallet:tx-otp:";
    public static final int VERIFICATION_CODE_ORIGIN = 100000;
    public static final int VERIFICATION_CODE_BOUND = 900000;
    public static final int ACCOUNT_UNLOCK_CODE_ORIGIN = 100000;
    public static final int ACCOUNT_UNLOCK_CODE_BOUND = 900000;
    public static final int PASSWORD_RESET_CODE_ORIGIN = 100000;
    public static final int PASSWORD_RESET_CODE_BOUND = 900000;
    public static final int WALLET_OTP_CODE_ORIGIN = 100000;
    public static final int WALLET_OTP_CODE_BOUND = 900000;
    public static final int WALLET_PIN_LENGTH = 6;
    public static final String WALLET_PIN_PATTERN = "^\\d{6}$";
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
    public static final String ERROR_RECIPIENT_WALLET_RISK = "RECIPIENT_WALLET_RISK";
    public static final String ERROR_BANK_NOT_ACTIVE = "BANK_NOT_ACTIVE";
    public static final String ERROR_BANK_ACCOUNT_NOT_ACTIVE = "BANK_ACCOUNT_NOT_ACTIVE";
    public static final String ERROR_BANK_ACCOUNT_HAS_PENDING_TRANSACTION = "BANK_ACCOUNT_HAS_PENDING_TRANSACTION";
    public static final String ERROR_WALLET_TRANSACTION_AMOUNT_LIMIT_EXCEEDED = "WALLET_TRANSACTION_AMOUNT_LIMIT_EXCEEDED";
    public static final String ERROR_PIN_REQUIRED = "PIN_REQUIRED";
    public static final String ERROR_INVALID_PIN = "INVALID_PIN";
    public static final String ERROR_PIN_LOCKED = "PIN_LOCKED";
    public static final String ERROR_INVALID_PIN_FORMAT = "INVALID_PIN_FORMAT";
    public static final String ERROR_PIN_OTP_REQUIRED = "PIN_OTP_REQUIRED";
    public static final String ERROR_INVALID_PIN_OTP = "INVALID_PIN_OTP";
    public static final String ERROR_PIN_ALREADY_EXISTS = "PIN_ALREADY_EXISTS";
    public static final String ERROR_TRANSACTION_OTP_REQUIRED = "TRANSACTION_OTP_REQUIRED";
    public static final String ERROR_INVALID_TRANSACTION_OTP = "INVALID_TRANSACTION_OTP";

    public static final String MESSAGE_EMAIL_ALREADY_EXISTS = "Email nay da duoc dang ky. Vui long dung email khac hoac dang nhap.";
    public static final String MESSAGE_PHONE_NUMBER_ALREADY_EXISTS = "So dien thoai nay da duoc dang ky. Vui long thu so khac hoac dang nhap.";
    public static final String MESSAGE_DEFAULT_ROLE_NOT_FOUND = "He thong dang gap su co. Vui long thu lai sau hoac lien he ho tro.";
    public static final String MESSAGE_INVALID_PHONE_OR_PASSWORD = "So dien thoai hoac mat khau khong dung. Vui long kiem tra lai.";
    public static final String MESSAGE_EMAIL_NOT_VERIFIED = "Email chua duoc xac thuc. Vui long kiem tra hop thu va xac thuc truoc khi dang nhap.";
    public static final String MESSAGE_ACCOUNT_LOCKED = "Tai khoan bi khoa. Vui long nhap mat khau va ma xac thuc da gui toi email de mo khoa.";
    public static final String MESSAGE_ACCOUNT_LOCKED_AFTER_FAILED_ATTEMPTS = "Tai khoan bi khoa do nhap sai mat khau qua nhieu lan. Ma mo khoa da duoc gui toi email cua ban.";
    public static final String MESSAGE_INVALID_VERIFICATION_CODE = "Ma xac thuc email khong hop le hoac da het han. Vui long yeu cau ma moi.";
    public static final String MESSAGE_INVALID_UNLOCK_CODE = "Ma mo khoa tai khoan khong hop le hoac da het han.";
    public static final String MESSAGE_INVALID_PASSWORD_RESET_CODE = "Ma dat lai mat khau khong hop le hoac da het han.";
    public static final String MESSAGE_USER_NOT_FOUND = "Khong tim thay tai khoan. Vui long kiem tra lai thong tin.";
    public static final String MESSAGE_INVALID_TOKEN = "Phien dang nhap khong hop le. Vui long dang nhap lai.";
    public static final String MESSAGE_VERIFICATION_CODE_SEND_COOLDOWN = "Vui long cho truoc khi yeu cau ma xac thuc moi.";
    public static final String MESSAGE_VERIFICATION_CODE_SEND_LIMIT_EXCEEDED = "Ban da gui yeu cau qua nhieu lan. Vui long thu lai sau.";
    public static final String MESSAGE_WALLET_NOT_FOUND = "Khong tim thay vi cua ban. Vui long lien he ho tro.";
    public static final String MESSAGE_BANK_NOT_FOUND = "Khong tim thay ngan hang hoac so tai khoan. Vui long kiem tra lai.";
    public static final String MESSAGE_BANK_ACCOUNT_NOT_FOUND = "Khong tim thay tai khoan ngan hang da lien ket.";
    public static final String MESSAGE_BANK_ACCOUNT_ALREADY_LINKED = "Tai khoan ngan hang nay da duoc lien ket voi vi.";
    public static final String MESSAGE_INSUFFICIENT_WALLET_BALANCE = "So du vi khong du de thuc hien giao dich. Vui long nap them tien.";
    public static final String MESSAGE_INSUFFICIENT_BANK_BALANCE = "So du tai khoan ngan hang khong du de thuc hien giao dich.";
    public static final String MESSAGE_DUPLICATED_TRANSACTION = "Giao dich nay da duoc xu ly truoc do.";
    public static final String MESSAGE_IDEMPOTENCY_PAYLOAD_MISMATCH = "Ma giao dich da duoc dung voi thong tin chuyen khoan khac. Vui long tao giao dich moi.";
    public static final String MESSAGE_TRANSACTION_NOT_FOUND = "Khong tim thay giao dich.";
    public static final String MESSAGE_RECIPIENT_NOT_FOUND = "Khong tim thay nguoi nhan. Vui long kiem tra lai so dien thoai.";
    public static final String MESSAGE_SELF_WALLET_TRANSFER = "Ban khong the chuyen tien cho chinh minh.";
    public static final String MESSAGE_USER_NOT_ACTIVE = "Tai khoan cua ban chua duoc kich hoat hoac da bi khoa.";
    public static final String MESSAGE_WALLET_NOT_ACTIVE = "Vi cua ban hien khong hoat dong. Vui long lien he ho tro.";
    public static final String MESSAGE_RECIPIENT_WALLET_RISK = "Khong the chuyen tien den tai khoan nay do tai khoan nguoi nhan da bi vi pham quy dinh. Vui long kiem tra lai.";
    public static final String MESSAGE_BANK_NOT_ACTIVE = "Ngan hang nay hien khong kha dung. Vui long thu ngan hang khac.";
    public static final String MESSAGE_BANK_ACCOUNT_NOT_ACTIVE = "Tai khoan ngan hang nay hien khong hoat dong.";
    public static final String MESSAGE_BANK_ACCOUNT_HAS_PENDING_TRANSACTION = "Khong the huy lien ket khi dang co giao dich cho xu ly.";
    public static final String MESSAGE_WALLET_TRANSACTION_AMOUNT_POSITIVE = "So tien phai lon hon 0.";
    public static final String MESSAGE_WALLET_TRANSACTION_AMOUNT_INTEGER = "So tien phai la so nguyen (khong co phan thap phan).";
    public static final String MESSAGE_WALLET_TRANSACTION_AMOUNT_MAX_PER_TRANSACTION = "So tien moi giao dich khong duoc vuot qua 50,000,000 dong.";
    public static final String MESSAGE_WALLET_TRANSACTION_DAILY_LIMIT_EXCEEDED = "Ban da vuot han muc giao dich trong ngay (100,000,000 dong). Vui long thu lai vao ngay mai.";
    public static final String MESSAGE_WALLET_TRANSACTION_MONTHLY_LIMIT_EXCEEDED = "Ban da vuot han muc giao dich trong thang theo cap han muc hien tai cua vi. Vui long thu lai vao thang sau hoac lien he ho tro de nang cap han muc.";
    public static final String MESSAGE_BUILD_VERIFICATION_EMAIL_FAILED = "He thong dang gap su co khi gui email. Vui long thu lai sau.";
    public static final String MESSAGE_LOAD_VERIFICATION_EMAIL_TEMPLATE_FAILED = "He thong dang gap su co khi tai mau email. Vui long thu lai sau.";
    public static final String MESSAGE_PIN_REQUIRED = "Giao dich nay yeu cau ma PIN. Vui long nhap ma PIN cua ban.";
    public static final String MESSAGE_INVALID_PIN = "Ma PIN khong dung. Vui long kiem tra lai.";
    public static final String MESSAGE_PIN_LOCKED = "Ma PIN bi khoa do nhap sai qua nhieu lan. Vui long tao lai ma PIN.";
    public static final String MESSAGE_INVALID_PIN_FORMAT = "Ma PIN phai la 6 chu so.";
    public static final String MESSAGE_PIN_OTP_REQUIRED = "Vui long yeu cau va nhap ma OTP de tao ma PIN.";
    public static final String MESSAGE_INVALID_PIN_OTP = "Ma OTP xac nhan PIN khong hop le hoac da het han.";
    public static final String MESSAGE_PIN_ALREADY_EXISTS = "Ban da co ma PIN. Vui long dung ma PIN hien tai de xac thuc giao dich.";
    public static final String MESSAGE_TRANSACTION_OTP_REQUIRED = "Vui long yeu cau va nhap ma OTP de xac thuc giao dich.";
    public static final String MESSAGE_INVALID_TRANSACTION_OTP = "Ma OTP xac thuc giao dich khong hop le hoac da het han.";
    public static final String MESSAGE_PIN_OTP_SENT = "Ma OTP xac nhan tao PIN da duoc gui toi email cua ban.";
    public static final String MESSAGE_PIN_CREATED = "Tao ma PIN thanh cong.";
    public static final String MESSAGE_TRANSACTION_OTP_SENT = "Ma OTP xac thuc giao dich da duoc gui toi email cua ban.";
    public static final String PHONE_NUMBER_NUMERIC_PATTERN = "^\\d+$";
    public static final String PHONE_NUMBER_PATTERN = "^\\d{10}$";
    public static final String MESSAGE_PHONE_NUMBER_EXACTLY_10_DIGITS = "So dien thoai phai gom dung 10 chu so.";
    public static final String MESSAGE_PHONE_NUMBER_NUMERIC_ONLY = "So dien thoai chi duoc chua chu so, khong duoc chua chu cai hoac ky tu dac biet.";
    public static final String MESSAGE_ACCOUNT_UNLOCKED_SUCCESSFULLY = "Mo khoa tai khoan thanh cong.";
    public static final String MESSAGE_VERIFICATION_CODE_SENT_SUCCESSFULLY = "Ma xac thuc da duoc gui thanh cong.";
    public static final String MESSAGE_PASSWORD_RESET_CODE_SENT_SUCCESSFULLY = "Ma dat lai mat khau da duoc gui thanh cong.";
    public static final String MESSAGE_PASSWORD_RESET_SUCCESSFULLY = "Dat lai mat khau thanh cong.";
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
    // Giới hạn giao dịch tháng theo cấp hạn mức ví (limitation)
    public static final String WALLET_LIMITATION_LEVEL_1_MONTHLY_LIMIT = "100000000"; // Phổ thông
    public static final String WALLET_LIMITATION_LEVEL_2_MONTHLY_LIMIT = "500000000"; // Trung cấp
    public static final String WALLET_LIMITATION_LEVEL_3_MONTHLY_LIMIT = "900000000"; // Cao cấp
    public static final int WALLET_LIMITATION_DEFAULT = 1;
    public static final String WALLET_PENDING_TIMEOUT_MINUTES_PROPERTY = "${application.wallet.pending-timeout-minutes:30}";

    // QR Code
    public static final int WALLET_QR_SIZE_PX = 300;
    public static final String ERROR_QR_INVALID = "QR_INVALID";
    public static final String MESSAGE_QR_INVALID = "Ma QR khong hop le. Vui long thu lai.";
    public static final String ERROR_QR_GENERATE_FAILED = "QR_GENERATE_FAILED";
    public static final String MESSAGE_QR_GENERATE_FAILED = "Khong the tao ma QR. Vui long thu lai sau.";
}
