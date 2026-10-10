package com.serviceco.serviceco_provider_service.services;

import com.serviceco.serviceco_provider_service.exception.ProviderNotFoundException;
import com.serviceco.serviceco_provider_service.model.entity.Provider;
import com.serviceco.serviceco_provider_service.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProviderOwnershipService {

    private final ProviderRepository providerRepository;

    public void verifyOwnershipOrAdmin(Long providerId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new AccessDeniedException(
                    "Authentication is required"
            );
        }

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return;
        }

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException(
                    "Authenticated JWT is required"
            );
        }

        Long userId = Long.valueOf(jwt.getSubject());

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() ->
                        new ProviderNotFoundException(
                                "Provider not found with id: " + providerId
                        ));

        if (!Objects.equals(provider.getUserId(), userId)) {
            throw new AccessDeniedException(
                    "You are not authorized to modify this provider profile"
            );
        }
    }
}