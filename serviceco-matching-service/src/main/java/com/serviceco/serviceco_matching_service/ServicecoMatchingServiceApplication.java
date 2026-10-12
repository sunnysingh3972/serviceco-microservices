package com.serviceco.serviceco_matching_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients 
public class ServicecoMatchingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicecoMatchingServiceApplication.class, args);
	}

}
