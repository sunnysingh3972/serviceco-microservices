package com.serviceco.serviceco_auth_service.model.dto;

import com.serviceco.serviceco_auth_service.model.UserRole;

public record RegistrationResponse(
        Long id,
        String name,
        String email,
        UserRole role
) {
}