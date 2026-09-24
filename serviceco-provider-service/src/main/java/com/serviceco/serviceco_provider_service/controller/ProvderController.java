package com.serviceco.serviceco_provider_service.controller;

import com.serviceco.serviceco_provider_service.model.dto.AvailabilityUpdateRequest;
import com.serviceco.serviceco_provider_service.model.dto.ProviderRequest;
import com.serviceco.serviceco_provider_service.model.dto.ProviderResponse;
import com.serviceco.serviceco_provider_service.model.dto.ProviderSearchResponse;
import com.serviceco.serviceco_provider_service.services.ProviderServiceImpl;
import com.serviceco.serviceco_provider_service.utility.ProviderStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/providers")
public class ProvderController {
    private final ProviderServiceImpl providerService;
    @PostMapping
    public ResponseEntity<ProviderResponse> saveProvider(@Valid  @RequestBody ProviderRequest providerRequest){
        ProviderResponse providerResponse=providerService.createProvider(providerRequest);
        return  new ResponseEntity<>(providerResponse, HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProviderResponse> getProvider(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                providerService.getProvider(id)
        );
    }

    @GetMapping
    public ResponseEntity<Page<ProviderResponse>> getAllProviders(@RequestParam(required = false)
                                                                       String location,
                                                                  @PageableDefault(
                                                                           page = 0,
                                                                           size = 10
                                                                   )
                                                                   Pageable pageable) {

        return ResponseEntity.ok(
                providerService.getAllProviders(location,pageable)
        );
    }
    @GetMapping("/search")
    public ResponseEntity<Page<ProviderSearchResponse>> searchProviders(

            @RequestParam String location,

            @RequestParam String skill,

            @RequestParam(defaultValue = "AVAILABLE")
            ProviderStatus status,

            @PageableDefault(
                    page = 0,
                    size = 10
            )
            Pageable pageable) {

        return ResponseEntity.ok(
                providerService.searchProviders(
                        location,
                        skill,
                        status,
                        pageable
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProviderResponse> updateProvider(
            @PathVariable Long id,
            @Valid @RequestBody ProviderRequest request) {

        return ResponseEntity.ok(
                providerService.updateProvider(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvider(
            @PathVariable Long id) {

        providerService.deleteProvider(id);

        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/availability")
    public ResponseEntity<ProviderResponse> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityUpdateRequest request) {

        return ResponseEntity.ok(
                providerService.updateAvailability(id, request)
        );
    }
}
