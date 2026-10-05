package com.serviceco.serviceco_booking_service.model.dto;

import java.time.LocalDateTime;

public record ApiError(
        int status,
        String message,
        LocalDateTime timestamp
) {
}
