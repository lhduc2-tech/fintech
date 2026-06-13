package com.university.fintech.service.mail;

import com.university.fintech.domain.port.primary.AuthMailService;
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
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AuthMailServiceImpl implements AuthMailService {

    private final JavaMailSender mailSender;
    private final ResourceLoader resourceLoader;

    @Value(Constant.SPRING_MAIL_USERNAME_PROPERTY)
    private String from;

    @Override
    public void sendVerificationCode(String to, String fullName, String verificationCode) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, Constant.CHARSET_UTF_8);
            helper.setTo(to);
            if (from != null && !from.isBlank()) {
                helper.setFrom(from);
            }
            helper.setSubject(Constant.EMAIL_VERIFICATION_SUBJECT);
            helper.setText(buildVerificationTemplate(fullName, verificationCode), true);
        } catch (MessagingException e) {
            throw new IllegalStateException(Constant.MESSAGE_BUILD_VERIFICATION_EMAIL_FAILED, e);
        }
        mailSender.send(message);
    }

    @Override
    public void sendAccountUnlockCode(String to, String fullName, String unlockCode) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, Constant.CHARSET_UTF_8);
            helper.setTo(to);
            if (from != null && !from.isBlank()) {
                helper.setFrom(from);
            }
            helper.setSubject(Constant.ACCOUNT_UNLOCK_SUBJECT);
            helper.setText(buildAccountUnlockTemplate(fullName, unlockCode), true);
        } catch (MessagingException e) {
            throw new IllegalStateException(Constant.MESSAGE_BUILD_VERIFICATION_EMAIL_FAILED, e);
        }
        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetCode(String to, String fullName, String resetCode) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, Constant.CHARSET_UTF_8);
            helper.setTo(to);
            if (from != null && !from.isBlank()) {
                helper.setFrom(from);
            }
            helper.setSubject(Constant.PASSWORD_RESET_SUBJECT);
            helper.setText(buildPasswordResetTemplate(fullName, resetCode), true);
        } catch (MessagingException e) {
            throw new IllegalStateException(Constant.MESSAGE_BUILD_VERIFICATION_EMAIL_FAILED, e);
        }
        mailSender.send(message);
    }

    private String buildVerificationTemplate(String fullName, String verificationCode) {
        String displayName = fullName == null || fullName.isBlank() ? Constant.EMAIL_FALLBACK_DISPLAY_NAME : escapeHtml(fullName);
        return loadTemplate()
                .replace(Constant.EMAIL_TEMPLATE_FULL_NAME_PLACEHOLDER, displayName)
                .replace(Constant.EMAIL_TEMPLATE_VERIFICATION_CODE_PLACEHOLDER, escapeHtml(verificationCode));
    }

    private String loadTemplate() {
        return loadTemplate(Constant.EMAIL_VERIFICATION_TEMPLATE);
    }

    private String buildAccountUnlockTemplate(String fullName, String unlockCode) {
        String displayName = fullName == null || fullName.isBlank() ? Constant.EMAIL_FALLBACK_DISPLAY_NAME : escapeHtml(fullName);
        return loadTemplate(Constant.ACCOUNT_UNLOCK_TEMPLATE)
                .replace(Constant.EMAIL_TEMPLATE_FULL_NAME_PLACEHOLDER, displayName)
                .replace(Constant.EMAIL_TEMPLATE_UNLOCK_CODE_PLACEHOLDER, escapeHtml(unlockCode));
    }

    private String buildPasswordResetTemplate(String fullName, String resetCode) {
        String displayName = fullName == null || fullName.isBlank() ? Constant.EMAIL_FALLBACK_DISPLAY_NAME : escapeHtml(fullName);
        return loadTemplate(Constant.PASSWORD_RESET_TEMPLATE)
                .replace(Constant.EMAIL_TEMPLATE_FULL_NAME_PLACEHOLDER, displayName)
                .replace(Constant.EMAIL_TEMPLATE_RESET_CODE_PLACEHOLDER, escapeHtml(resetCode));
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

    private String escapeHtml(String value) {
        return value
                .replace(Constant.HTML_AMPERSAND, Constant.HTML_AMPERSAND_ESCAPED)
                .replace(Constant.HTML_LESS_THAN, Constant.HTML_LESS_THAN_ESCAPED)
                .replace(Constant.HTML_GREATER_THAN, Constant.HTML_GREATER_THAN_ESCAPED)
                .replace(Constant.HTML_QUOTATION_MARK, Constant.HTML_QUOTATION_MARK_ESCAPED);
    }
}
