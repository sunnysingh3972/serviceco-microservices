package com.serviceco.serviceco_provider_service.model.dto;

import com.serviceco.serviceco_provider_service.utility.ProviderStatus;

import java.math.BigDecimal;

public record ProviderSummaryResponse(Long id,
                                      String name,
                                      String location,
                                      BigDecimal hourlyRate,
                                      Double rating,
                                      ProviderStatus status) {
}
