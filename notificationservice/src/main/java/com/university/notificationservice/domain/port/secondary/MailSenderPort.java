package com.university.notificationservice.domain.port.secondary;

/**
 * Secondary (driven) port gửi email HTML.
 */
public interface MailSenderPort {

    void sendHtml(String to, String subject, String html);
}
