package com.university.fintech.service.mail;

import com.university.fintech.domain.port.primary.WalletNotificationMailService;
import com.university.fintech.shared.constant.Constant;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class WalletNotificationMailServiceImpl implements WalletNotificationMailService {

    private static final Locale VIETNAM_LOCALE = new Locale("vi", "VN");

    private final JavaMailSender mailSender;
    private final ResourceLoader resourceLoader;

    @Value(Constant.SPRING_MAIL_USERNAME_PROPERTY)
    private String from;

    @Override
    public void sendTransactionNotification(
            String to,
            String fullName,
            String transactionType,
            BigDecimal amount,
            String status,
            String referenceCode,
            String description,
            String recipientFullName,
            String recipientPhoneNumber
    ) {
        sendHtml(
                to,
                Constant.WALLET_TRANSACTION_SUBJECT,
                buildTransactionTemplate(
                        fullName,
                        transactionType,
                        amount,
                        status,
                        referenceCode,
                        description,
                        recipientFullName,
                        recipientPhoneNumber
                )
        );
    }

    @Override
    public void sendTransactionErrorNotification(
            String to,
            String fullName,
            String transactionType,
            BigDecimal amount,
            String errorMessage
    ) {
        sendHtml(
                to,
                Constant.WALLET_TRANSACTION_ERROR_SUBJECT,
                buildTransactionErrorTemplate(fullName, transactionType, amount, errorMessage)
        );
    }

    private String buildTransactionTemplate(
            String fullName,
            String transactionType,
            BigDecimal amount,
            String status,
            String referenceCode,
            String description,
            String recipientFullName,
            String recipientPhoneNumber
    ) {
        return loadTemplate(Constant.WALLET_TRANSACTION_TEMPLATE)
                .replace(Constant.EMAIL_TEMPLATE_FULL_NAME_PLACEHOLDER, displayName(fullName))
                .replace(Constant.EMAIL_TEMPLATE_TRANSACTION_TYPE_PLACEHOLDER, escapeHtml(displayTransactionType(transactionType)))
                .replace(Constant.EMAIL_TEMPLATE_AMOUNT_PLACEHOLDER, escapeHtml(formatAmount(amount)))
                .replace(Constant.EMAIL_TEMPLATE_STATUS_PLACEHOLDER, escapeHtml(displayStatus(status)))
                .replace(Constant.EMAIL_TEMPLATE_STATUS_COLOR_PLACEHOLDER, statusColor(status))
                .replace(Constant.EMAIL_TEMPLATE_STATUS_BACKGROUND_PLACEHOLDER, statusBackground(status))
                .replace(Constant.EMAIL_TEMPLATE_REFERENCE_CODE_PLACEHOLDER, escapeHtml(referenceCode))
                .replace(Constant.EMAIL_TEMPLATE_DESCRIPTION_PLACEHOLDER, escapeHtml(description))
                .replace(
                        Constant.EMAIL_TEMPLATE_RECIPIENT_SECTION_PLACEHOLDER,
                        buildRecipientSection(recipientFullName, recipientPhoneNumber)
                );
    }

    private String buildTransactionErrorTemplate(
            String fullName,
            String transactionType,
            BigDecimal amount,
            String errorMessage
    ) {
        return loadTemplate(Constant.WALLET_TRANSACTION_ERROR_TEMPLATE)
                .replace(Constant.EMAIL_TEMPLATE_FULL_NAME_PLACEHOLDER, displayName(fullName))
                .replace(Constant.EMAIL_TEMPLATE_TRANSACTION_TYPE_PLACEHOLDER, escapeHtml(displayTransactionType(transactionType)))
                .replace(Constant.EMAIL_TEMPLATE_AMOUNT_PLACEHOLDER, escapeHtml(formatAmount(amount)))
                .replace(Constant.EMAIL_TEMPLATE_ERROR_MESSAGE_PLACEHOLDER, escapeHtml(errorMessage));
    }

    private void sendHtml(String to, String subject, String html) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, Constant.CHARSET_UTF_8);
            helper.setTo(to);
            if (from != null && !from.isBlank()) {
                helper.setFrom(from);
            }
            helper.setSubject(subject);
            helper.setText(html, true);
        } catch (MessagingException e) {
            throw new IllegalStateException("Could not build wallet notification email.", e);
        }
        mailSender.send(message);
    }

    private String loadTemplate(String template) {
        try {
            return StreamUtils.copyToString(
                    resourceLoader.getResource(template).getInputStream(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new IllegalStateException(Constant.MESSAGE_LOAD_VERIFICATION_EMAIL_TEMPLATE_FAILED, e);
        }
    }

    private String displayName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return Constant.EMAIL_FALLBACK_DISPLAY_NAME;
        }
        return escapeHtml(fullName);
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "N/A";
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(VIETNAM_LOCALE);
        return currencyFormat.format(amount);
    }

    private String displayTransactionType(String transactionType) {
        if (Constant.WALLET_TRANSACTION_TYPE_BANK_TO_WALLET.equals(transactionType)) {
            return "Nạp tiền vào ví";
        }
        if (Constant.WALLET_TRANSACTION_TYPE_WALLET_TO_BANK.equals(transactionType)) {
            return "Rút tiền về ngân hàng";
        }
        if (Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT.equals(transactionType)) {
            return "Chuyển tiền";
        }
        if (Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_IN.equals(transactionType)) {
            return "Nhận tiền";
        }
        return transactionType;
    }

    private String displayStatus(String status) {
        if (Constant.STATUS_SUCCESS.equals(status)) {
            return "Thành công";
        }
        if (Constant.STATUS_FAILED.equals(status)) {
            return "Thất bại";
        }
        if (Constant.STATUS_PENDING.equals(status)) {
            return "Đang xử lý";
        }
        if (Constant.STATUS_TIMED_OUT.equals(status)) {
            return "Hết thời gian xử lý";
        }
        return status;
    }

    private String buildRecipientSection(String recipientFullName, String recipientPhoneNumber) {
        if ((recipientFullName == null || recipientFullName.isBlank())
                && (recipientPhoneNumber == null || recipientPhoneNumber.isBlank())) {
            return "";
        }
        String safeFullName = recipientFullName == null || recipientFullName.isBlank()
                ? "Chưa có thông tin"
                : escapeHtml(recipientFullName);
        String safePhoneNumber = recipientPhoneNumber == null || recipientPhoneNumber.isBlank()
                ? "Chưa có thông tin"
                : escapeHtml(recipientPhoneNumber);
        return """
                <div style="border:1px solid #dfe7f1;border-radius:8px;padding:18px;margin:22px 0 0;background:#f8fbff;">
                  <div style="font-size:13px;color:#62748a;margin:0 0 12px;font-weight:bold;text-transform:uppercase;letter-spacing:.4px;">Thông tin người nhận</div>
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="border-collapse:collapse;font-size:14px;">
                    <tr>
                      <td style="padding:8px 0;border-bottom:1px solid #edf1f6;color:#62748a;width:38%%;">Họ tên</td>
                      <td style="padding:8px 0;border-bottom:1px solid #edf1f6;text-align:right;font-weight:bold;color:#172033;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:8px 0;color:#62748a;">Số điện thoại</td>
                      <td style="padding:8px 0;text-align:right;color:#172033;">%s</td>
                    </tr>
                  </table>
                </div>
                """.formatted(safeFullName, safePhoneNumber);
    }

    private String statusColor(String status) {
        if (Constant.STATUS_SUCCESS.equals(status)) {
            return "#166534";
        }
        if (Constant.STATUS_FAILED.equals(status)) {
            return "#991b1b";
        }
        if (Constant.STATUS_TIMED_OUT.equals(status)) {
            return "#7c3aed";
        }
        return "#92400e";
    }

    private String statusBackground(String status) {
        if (Constant.STATUS_SUCCESS.equals(status)) {
            return "#dcfce7";
        }
        if (Constant.STATUS_FAILED.equals(status)) {
            return "#fee2e2";
        }
        if (Constant.STATUS_TIMED_OUT.equals(status)) {
            return "#ede9fe";
        }
        return "#fef3c7";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace(Constant.HTML_AMPERSAND, Constant.HTML_AMPERSAND_ESCAPED)
                .replace(Constant.HTML_LESS_THAN, Constant.HTML_LESS_THAN_ESCAPED)
                .replace(Constant.HTML_GREATER_THAN, Constant.HTML_GREATER_THAN_ESCAPED)
                .replace(Constant.HTML_QUOTATION_MARK, Constant.HTML_QUOTATION_MARK_ESCAPED);
    }
}
