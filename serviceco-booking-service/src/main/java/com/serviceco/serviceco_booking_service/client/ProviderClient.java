package com.serviceco.serviceco_booking_service.client;

import com.serviceco.serviceco_booking_service.model.dto.ProviderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "serviceco-provider-service")
public interface ProviderClient {
    @GetMapping("/api/providers/{id}")
    ProviderResponse getProvider(@PathVariable("id") Long providerId);

}
