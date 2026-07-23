package com.university.coreservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * CoreService — service xử lý nghiệp vụ thanh toán.
 * Nhận request từ PaymentGateway (HTTP), xử lý giao dịch,
 * publish kết quả lên Kafka (payment.result) và mail topic (payment.mail).
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.university.coreservice",
        enableDefaultTransactions = false
)
public class CoreserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreserviceApplication.class, args);
    }
}
