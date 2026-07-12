package com.university.paymentgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.university.paymentgateway",
        enableDefaultTransactions = false
)
public class PaymentgatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentgatewayApplication.class, args);
    }

}
