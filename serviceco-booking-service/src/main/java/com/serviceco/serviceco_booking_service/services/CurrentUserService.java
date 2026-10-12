package com.serviceco.serviceco_booking_service.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();

        return Long.valueOf(jwt.getSubject());
    }
    public String getAuthorizationHeader() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException(
                    "Authenticated JWT is not available"
            );
        }

        return "Bearer " + jwt.getTokenValue();
    }
}
