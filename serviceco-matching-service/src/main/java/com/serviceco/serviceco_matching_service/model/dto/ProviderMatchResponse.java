
package com.serviceco.serviceco_matching_service.model.dto;

import java.math.BigDecimal;

public record ProviderMatchResponse(
        Long providerId,
        String name,
        String location,
        BigDecimal hourlyRate,
        Double rating,
        String status,
        int matchScore,
        String matchReason
) {
}