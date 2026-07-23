package com.university.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * NotificationService — phụ trách toàn bộ việc gửi mail thông báo giao dịch.
 * Consume từ Kafka topic payment.mail và gửi email cho người dùng.
 */
@SpringBootApplication
public class NotificationserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationserviceApplication.class, args);
    }
}
