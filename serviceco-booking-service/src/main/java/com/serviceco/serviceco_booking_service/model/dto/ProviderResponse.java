package com.serviceco.serviceco_booking_service.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProviderResponse(
        Long id,
        String name,
        String email,
        String phoneNumber,
        Integer experience,
        String location,
        BigDecimal hourlyRate,
        Double rating,
        String status,
        List<String> skills
) {
}