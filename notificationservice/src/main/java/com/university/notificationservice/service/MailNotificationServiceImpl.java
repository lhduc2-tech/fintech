package com.university.notificationservice.service;

import com.university.notificationservice.domain.model.MailNotificationMessage;
import com.university.notificationservice.domain.port.primary.MailNotificationService;
import com.university.notificationservice.domain.port.secondary.MailSenderPort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service implementation cho Primary Port MailNotificationService.
 * Phụ trách render HTML template và gửi qua Secondary Port MailSenderPort.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailNotificationServiceImpl implements MailNotificationService {

    private static final Locale VIETNAM_LOCALE = new Locale("vi", "VN");
    private static final String WALLET_TRANSACTION_TEMPLATE = "classpath:template/wallet-transaction-email.html";
    private static final String WALLET_TRANSACTION_SUBJECT = "Thông báo giao dịch ví Fintech";

    private static final String PH_FULL_NAME       = "{{fullName}}";
    private static final String PH_TRANSACTION_TYPE = "{{transactionType}}";
    private static final String PH_AMOUNT           = "{{amount}}";
    private static final String PH_STATUS           = "{{status}}";
    private static final String PH_STATUS_COLOR     = "{{statusColor}}";
    private static final String PH_STATUS_BACKGROUND = "{{statusBackground}}";
    private static final String PH_REFERENCE_CODE   = "{{referenceCode}}";
    private static final String PH_DESCRIPTION      = "{{description}}";
    private static final String PH_RECIPIENT_SECTION = "{{recipientSection}}";

    private final MailSenderPort mailSenderPort;
    private final ResourceLoader resourceLoader;
    private final Map<String, String> templateCache = new ConcurrentHashMap<>();
    private final Set<String> processedMailKeys = Collections.newSetFromMap(
            Collections.synchronizedMap(new LinkedHashMap<String, Boolean>(1000, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                    return size() > 5000;
                }
            })
    );

    @PostConstruct
    public void initTemplateCache() {
        try {
            log.info("Pre-loading email templates into memory cache...");
            loadTemplate(WALLET_TRANSACTION_TEMPLATE);
            log.info("Email templates pre-loaded successfully.");
        } catch (Exception e) {
            log.warn("Failed to pre-load email templates: {}", e.getMessage());
        }
    }

    @Override
    public void sendTransactionNotification(MailNotificationMessage message) {
        if (message.getTo() == null || message.getTo().isBlank()) {
            log.warn("Cannot send mail: empty email address for referenceCode={}", message.getReferenceCode());
            return;
        }

        String mailKey = message.getReferenceCode() + ":" + message.getStatus();
        if (!processedMailKeys.add(mailKey)) {
            log.warn("Duplicate mail notification skipped: referenceCode={}, status={}",
                    message.getReferenceCode(), message.getStatus());
            return;
        }

        String html = buildTransactionTemplate(message);
        mailSenderPort.sendHtml(message.getTo(), WALLET_TRANSACTION_SUBJECT, html);
    }

    private String buildTransactionTemplate(MailNotificationMessage msg) {
        return loadTemplate(WALLET_TRANSACTION_TEMPLATE)
                .replace(PH_FULL_NAME, displayName(msg.getFullName()))
                .replace(PH_TRANSACTION_TYPE, escapeHtml(displayTransactionType(msg.getTransactionType())))
                .replace(PH_AMOUNT, escapeHtml(formatAmount(msg.getAmount())))
                .replace(PH_STATUS, escapeHtml(displayStatus(msg.getStatus())))
                .replace(PH_STATUS_COLOR, statusColor(msg.getStatus()))
                .replace(PH_STATUS_BACKGROUND, statusBackground(msg.getStatus()))
                .replace(PH_REFERENCE_CODE, escapeHtml(nullSafe(msg.getReferenceCode())))
                .replace(PH_DESCRIPTION, escapeHtml(nullSafe(msg.getDescription())))
                .replace(PH_RECIPIENT_SECTION, buildRecipientSection(msg.getRecipientFullName(), msg.getRecipientPhoneNumber()));
    }

    private String loadTemplate(String template) {
        return templateCache.computeIfAbsent(template, t -> {
            try {
                return StreamUtils.copyToString(
                        resourceLoader.getResource(t).getInputStream(),
                        StandardCharsets.UTF_8
                );
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load email template: " + t, e);
            }
        });
    }

    private String buildRecipientSection(String recipientFullName, String recipientPhoneNumber) {
        if ((recipientFullName == null || recipientFullName.isBlank())
                && (recipientPhoneNumber == null || recipientPhoneNumber.isBlank())) {
            return "";
        }
        String safeFullName = recipientFullName == null || recipientFullName.isBlank()
                ? "Chưa có thông tin" : escapeHtml(recipientFullName);
        String safePhone = recipientPhoneNumber == null || recipientPhoneNumber.isBlank()
                ? "Chưa có thông tin" : escapeHtml(recipientPhoneNumber);
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
                """.formatted(safeFullName, safePhone);
    }

    private String displayName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "bạn";
        return escapeHtml(fullName);
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "N/A";
        return NumberFormat.getCurrencyInstance(VIETNAM_LOCALE).format(amount);
    }

    private String displayTransactionType(String type) {
        if (type == null) return "";
        return switch (type) {
            case "BANK_TO_WALLET"     -> "Nạp tiền vào ví";
            case "WALLET_TO_BANK"     -> "Rút tiền về ngân hàng";
            case "WALLET_TRANSFER_OUT" -> "Chuyển tiền";
            case "WALLET_TRANSFER_IN"  -> "Nhận tiền";
            default -> type;
        };
    }

    private String displayStatus(String status) {
        if (status == null) return "";
        return switch (status) {
            case "SUCCESS"   -> "Thành công";
            case "FAILED"    -> "Thất bại";
            case "PENDING"   -> "Đang xử lý";
            case "TIMED_OUT" -> "Hết thời gian xử lý";
            default -> status;
        };
    }

    private String statusColor(String status) {
        if ("SUCCESS".equals(status)) return "#166534";
        if ("FAILED".equals(status))  return "#991b1b";
        if ("TIMED_OUT".equals(status)) return "#7c3aed";
        return "#92400e";
    }

    private String statusBackground(String status) {
        if ("SUCCESS".equals(status)) return "#dcfce7";
        if ("FAILED".equals(status))  return "#fee2e2";
        if ("TIMED_OUT".equals(status)) return "#ede9fe";
        return "#fef3c7";
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
