package com.app.spring.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

// Tells Spring Boot to turn off its default, automatic database connection setup
//@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@SpringBootApplication()
public class SpringSecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurityApplication.class, args);
	}

}
