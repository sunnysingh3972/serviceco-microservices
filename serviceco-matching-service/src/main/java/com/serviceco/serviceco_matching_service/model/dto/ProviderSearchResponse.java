package com.serviceco.serviceco_matching_service.model.dto;

import java.math.BigDecimal;

/**
 * ProviderSearchResponse
 */
public record ProviderSearchResponse(
 Long id,
        String name,
        String location,
        BigDecimal hourlyRate,
        Double rating,
        String status
) {

}
