package com.serviceco.serviceco_booking_service.model.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BookingRequest(

        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotNull(message = "Provider ID is required")
        Long providerId,

        @NotBlank(message = "Service skill is required")
        String serviceSkill,

        @NotNull(message = "Booking date is required")
        @Future(message = "Booking date must be in the future")
        LocalDateTime bookingDate,

        @NotNull(message = "Duration is required")
        @Min(value = 1, message = "Duration must be at least 1 hour")
        Integer durationHours
) {
}