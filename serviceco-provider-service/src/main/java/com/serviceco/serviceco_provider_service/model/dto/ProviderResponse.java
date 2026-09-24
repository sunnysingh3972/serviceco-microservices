package com.serviceco.serviceco_provider_service.model.dto;

import com.serviceco.serviceco_provider_service.utility.ProviderStatus;

import java.math.BigDecimal;
import java.util.List;

public record ProviderResponse(

        Long id,

        String name,

        String email,

        String phone,

        Integer experience,

        String location,

        BigDecimal hourlyRate,

        Double rating,

        ProviderStatus status,
        List<String> skills
) {
}
