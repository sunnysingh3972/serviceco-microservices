package com.serviceco.serviceco_booking_service.services;

import com.serviceco.serviceco_booking_service.client.ProviderClient;
import com.serviceco.serviceco_booking_service.model.dto.ProviderResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProviderOwnershipService {

    private final ProviderClient providerClient;
    private final CurrentUserService currentUserService;

    public void verifyProviderOwnership(Long bookingProviderId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // Admin can manage bookings without owning the provider profile
        boolean isAdmin = authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return;
        }

        // Fetch the logged-in user's provider profile
        ProviderResponse currentProvider =
                providerClient.getCurrentProvider(
                        currentUserService.getAuthorizationHeader()
                );

        // Compare Provider ID, not Auth User ID
        if (!Objects.equals(
                currentProvider.id(),
                bookingProviderId)) {

            throw new AccessDeniedException(
                    "You are not authorized to manage this booking"
            );
        }
    }
}
