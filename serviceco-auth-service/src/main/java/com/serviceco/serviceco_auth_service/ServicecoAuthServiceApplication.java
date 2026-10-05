package com.serviceco.serviceco_auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ServicecoAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicecoAuthServiceApplication.class, args);
	}

}
