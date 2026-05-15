package com.university.fintech.wire.database;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {"com.university.fintech"}, enableDefaultTransactions = false)
public class DatabaseConfiguration {

    private static final String SYSTEM_AUDITOR = "system";

    private final DataSource dataSource;

    @Autowired
    public DatabaseConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void logDataSourceDetails() {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            log.info("HikariCP DataSource Details:");
            log.info("JDBC URL: {}", hikariDataSource.getJdbcUrl());
            log.info("Username: {}", hikariDataSource.getUsername());
            log.info("Maximum Pool Size: {}", hikariDataSource.getMaximumPoolSize());
            log.info("Minimum Idle Connections: {}", hikariDataSource.getMinimumIdle());
        } else {
            log.info("DataSource is not an instance of HikariDataSource");
        }
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(authentication -> authentication.isAuthenticated()
                        && !(authentication instanceof AnonymousAuthenticationToken))
                .map(Authentication::getPrincipal)
                .map(this::resolveAuditor)
                .filter(value -> !value.isBlank())
                .or(() -> Optional.of(SYSTEM_AUDITOR));
    }

    private String resolveAuditor(Object principal) {
        if (principal instanceof Map<?, ?> map) {
            Object username = map.get("username");
            return username == null ? "" : username.toString();
        }

        return principal == null ? "" : principal.toString();
    }
}