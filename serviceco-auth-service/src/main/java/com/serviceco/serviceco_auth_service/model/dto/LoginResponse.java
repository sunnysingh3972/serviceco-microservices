package com.serviceco.serviceco_auth_service.model.dto;

public record LoginResponse(
        String accessToken,
                             String tokenType,
                             Long userId,
                             String name,
                             String email,
                             String role
) {
}
