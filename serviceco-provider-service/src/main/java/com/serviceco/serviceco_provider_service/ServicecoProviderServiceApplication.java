package com.serviceco.serviceco_provider_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ServicecoProviderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicecoProviderServiceApplication.class, args);
	}

}
