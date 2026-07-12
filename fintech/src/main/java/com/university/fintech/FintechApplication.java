package com.university.fintech;

import com.university.fintech.shared.generation.domain.ExcludeFromGeneratedCodeCoverage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication(scanBasePackages = "com.university", exclude = {UserDetailsServiceAutoConfiguration.class})
@ExcludeFromGeneratedCodeCoverage(reason = "Not testing logs")
public class FintechApplication {

	public static void main(String[] args) {
		Environment env = SpringApplication.run(FintechApplication.class, args).getEnvironment();

		if (log.isInfoEnabled()) {
			log.info(ApplicationStartupTraces.of(env));
		}
	}

}