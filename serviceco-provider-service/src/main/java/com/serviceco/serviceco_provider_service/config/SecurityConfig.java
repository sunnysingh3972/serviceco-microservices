
package com.serviceco.serviceco_provider_service.config;

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

        return new SecretKeySpec(decodedKey, "HmacSHA256");
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSigningKey) {
        return NimbusJwtDecoder
                .withSecretKey(jwtSigningKey)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter);

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Swagger documentation
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Current provider profile requires PROVIDER role
                        .requestMatchers(
                                HttpMethod.GET, "/api/providers/me"
                        ).hasRole("PROVIDER")

                        // Public provider browsing and search
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/providers/search",
                                "/api/providers",
                                "/api/providers/*",
                                "/api/providers/*/skills"
                        ).permitAll()

                        // Creating a provider profile
                        .requestMatchers(
                                HttpMethod.POST, "/api/providers"
                        ).hasRole("PROVIDER")

                        // Provider management operations
                        .requestMatchers(
                                HttpMethod.PUT, "/api/providers/*"
                        ).hasAnyRole("PROVIDER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/providers/*/availability"
                        ).hasAnyRole("PROVIDER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE, "/api/providers/*"
                        ).hasAnyRole("PROVIDER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.POST, "/api/providers/*/skills"
                        ).hasAnyRole("PROVIDER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/providers/*/skills/*"
                        ).hasAnyRole("PROVIDER", "ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }
}
