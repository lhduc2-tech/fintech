package com.university.paymentgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PaymentGateway — service trung gian thuần túy.
 * Nhận yêu cầu từ Kafka (payment.request), chuyển tiếp đến CoreService qua HTTP.
 */
@SpringBootApplication
public class PaymentgatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentgatewayApplication.class, args);
    }

}

