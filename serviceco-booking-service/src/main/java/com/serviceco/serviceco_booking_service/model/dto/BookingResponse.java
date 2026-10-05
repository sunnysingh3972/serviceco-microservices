package com.serviceco.serviceco_booking_service.model.dto;

import com.serviceco.serviceco_booking_service.model.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(

        Long id,
        Long customerId,
        Long providerId,
        String serviceSkill,
        LocalDateTime bookingDate,
        LocalDateTime bookingEnd,
        Integer durationHours,
        BigDecimal hourlyRate,
        BigDecimal totalAmount,
        BookingStatus status,
        LocalDateTime createdAt
) {
}