package com.serviceco.serviceco_auth_service.model.dto;

import java.time.LocalDateTime;

public record ApiError(
        int status,
        String message,
        LocalDateTime timestamp
) {
}