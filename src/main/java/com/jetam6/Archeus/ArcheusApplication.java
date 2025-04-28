package com.jetam6.Archeus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ArcheusApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArcheusApplication.class, args);
	}

}
