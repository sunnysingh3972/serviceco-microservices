package com.serviceco.serviceco_provider_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI serviceCoOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("ServiceCo Provider Service API")
                                .description(
                                        "REST APIs for managing service "
                                                + "providers, skills, availability "
                                                + "and provider search."
                                )
                                .version("1.0.0")
                );
    }
}
