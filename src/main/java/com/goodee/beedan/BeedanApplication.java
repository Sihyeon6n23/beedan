package com.goodee.beedan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BeedanApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeedanApplication.class, args);
	}

}
