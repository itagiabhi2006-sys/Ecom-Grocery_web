package com.zsecurity.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GroceryWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GroceryWebApplication.class, args);
	}

}
