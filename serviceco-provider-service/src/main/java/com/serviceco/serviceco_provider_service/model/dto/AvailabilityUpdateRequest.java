package com.serviceco.serviceco_provider_service.model.dto;

import com.serviceco.serviceco_provider_service.utility.ProviderStatus;
import jakarta.validation.constraints.NotNull;

public record AvailabilityUpdateRequest(@NotNull(message = "Status is required")
                                        ProviderStatus status) {
}
