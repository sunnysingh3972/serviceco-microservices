package com.serviceco.serviceco_matching_service.client;

import com.serviceco.serviceco_matching_service.exception.ProviderServiceUnavailableException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class ProviderClientFallbackFactory
        implements FallbackFactory<ProviderClient> {

    @Override
    public ProviderClient create(Throwable cause) {

        return (location, skill, status, page, size) -> {
            throw new ProviderServiceUnavailableException(
                    "Provider Service is currently unavailable. Please try again later.",
                    cause
            );
        };
    }
}