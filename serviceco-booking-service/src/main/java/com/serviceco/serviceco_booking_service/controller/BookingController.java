package com.serviceco.serviceco_booking_service.controller;

import com.serviceco.serviceco_booking_service.client.ProviderClient;
import com.serviceco.serviceco_booking_service.model.dto.ProviderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final ProviderClient providerClient;

    @GetMapping("/providers/{providerId}")
    public ProviderResponse getProvider(@PathVariable Long providerId) {
        return providerClient.getProvider(providerId);
    }
}