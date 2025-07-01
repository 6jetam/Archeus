package com.jetam6;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.jetam6")
@EnableJpaRepositories(basePackages = "com.jetam6.ArcheusRepository")
@EntityScan(basePackages = "com.jetam6.ArcheusModel")  
@EnableScheduling
public class ArcheusApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArcheusApplication.class, args);
	}
}
