package com.university.notificationservice.infrastructure.secondary.mail;

import com.university.notificationservice.domain.port.secondary.MailSenderPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Driven Adapter (Secondary Infrastructure) gửi HTML mail qua JavaMailSender.
 * Sử dụng Java 21 Virtual Threads để dispatch mail bất đồng bộ không làm nghẽn Kafka Consumer.
 */
@Slf4j
@Component
public class JavaMailSenderAdapter implements MailSenderPort {

    private final JavaMailSender mailSender;
    private final ExecutorService mailExecutor = Executors.newCachedThreadPool();


    @Value("${application.mail.from:}")
    private String from;

    public JavaMailSenderAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendHtml(String to, String subject, String html) {
        mailExecutor.submit(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
                helper.setTo(to);
                if (from != null && !from.isBlank()) {
                    helper.setFrom(from);
                }
                helper.setSubject(subject);
                helper.setText(html, true);

                mailSender.send(message);
                log.info("Sent email successfully (async) to={}", to);
            } catch (Exception e) {
                log.error("Failed to send email to={}: {}", to, e.getMessage(), e);
            }
        });
    }
}

