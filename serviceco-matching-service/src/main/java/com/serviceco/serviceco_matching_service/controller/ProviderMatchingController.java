
package com.serviceco.serviceco_matching_service.controller;

import com.serviceco.serviceco_matching_service.model.dto.ProviderMatchPageResponse;
import com.serviceco.serviceco_matching_service.services.ProviderMatchingService;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matching")
@Validated
public class ProviderMatchingController {

        private final ProviderMatchingService providerMatchingService;

        public ProviderMatchingController(ProviderMatchingService providerMatchingService) {
                this.providerMatchingService = providerMatchingService;
        }

        @GetMapping("/providers")
        public ResponseEntity<ProviderMatchPageResponse> findMatchingProviders(
                        @RequestParam @NotBlank String location,
                        @RequestParam @NotBlank String skill,
                        @RequestParam(defaultValue = "AVAILABLE") String status,
                        @RequestParam(required = false) @DecimalMin(value = "0.01", message = "Maximum hourly rate must be greater than zero") BigDecimal maxHourlyRate,
                        @RequestParam(defaultValue = "0") @Min(0) int page,
                        @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

                ProviderMatchPageResponse response = providerMatchingService.findMatchingProviders(
                                location.trim(),
                                skill.trim(),
                                status.trim(),
                                maxHourlyRate,
                                page,
                                size);

                return ResponseEntity.ok(response);
        }
}