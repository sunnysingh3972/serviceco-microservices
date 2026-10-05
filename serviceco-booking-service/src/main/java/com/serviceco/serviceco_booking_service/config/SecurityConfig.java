package com.serviceco.serviceco_booking_service.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class SecurityConfig {

    @Bean
    public SecretKey jwtSigningKey(
            @Value("${jwt.secret}") String secret) {

        byte[] decodedKey = Base64.getDecoder().decode(secret);

        return new SecretKeySpec(
                decodedKey,
                "HmacSHA256"
        );
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSigningKey) {

        return NimbusJwtDecoder
                .withSecretKey(jwtSigningKey)
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        ))
                .authorizeHttpRequests(auth -> auth

                        // Anyone logged in can view bookings
                        .requestMatchers(HttpMethod.GET, "/api/bookings/**")
                        .authenticated()

                        // Customer creates a booking
                        .requestMatchers(HttpMethod.POST, "/api/bookings")
                        .hasAnyRole("CUSTOMER", "ADMIN")

                        // Provider accepts/rejects/completes
                        .requestMatchers(HttpMethod.PATCH, "/api/bookings/*/accept")
                        .hasAnyRole("PROVIDER", "ADMIN")

                        .requestMatchers(HttpMethod.PATCH, "/api/bookings/*/reject")
                        .hasAnyRole("PROVIDER", "ADMIN")

                        .requestMatchers(HttpMethod.PATCH, "/api/bookings/*/complete")
                        .hasAnyRole("PROVIDER", "ADMIN")

                        // Customer cancels
                        .requestMatchers(HttpMethod.PATCH, "/api/bookings/*/cancel")
                        .hasAnyRole("CUSTOMER", "ADMIN")

                        // Everything else requires login
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {})
                );

        return http.build();
    }
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return converter;
    }
}