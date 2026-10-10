package com.serviceco.serviceco_provider_service.controller;

import com.serviceco.serviceco_provider_service.model.dto.*;
import com.serviceco.serviceco_provider_service.services.ProviderServiceImpl;
import com.serviceco.serviceco_provider_service.utility.ProviderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
@Tag(
        name = "Provider APIs",
        description = "APIs for managing service providers"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/providers")
public class ProvderController {
    private final ProviderServiceImpl providerService;
    @Operation(
            summary = "Create a new provider",
            description = "Creates a new service provider with the provided details."
    )
    @PostMapping
    public ResponseEntity<ProviderResponse> saveProvider( @AuthenticationPrincipal Jwt jwt,@Valid  @RequestBody ProviderRequest providerRequest){
        Long userId = Long.valueOf(jwt.getSubject());
        ProviderResponse providerResponse=providerService.createProvider(providerRequest,userId);
        return  new ResponseEntity<>(providerResponse, HttpStatus.CREATED);
    }
    @Operation(
            summary = "Get provider by ID",
            description = "Retrieves a service provider by their unique ID."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProviderResponse> getProvider(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                providerService.getProvider(id)
        );
    }
    @Operation(
            summary = "Get all providers",
            description = "Retrieves a paginated list of all service providers, optionally filtered by location."
    )
    @GetMapping
    public ResponseEntity<Page<ProviderSummaryResponse>> getAllProviders(@RequestParam(required = false)
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
    @Operation(
            summary = "Search providers",
            description = "Searches for service providers based on location, skill, and availability status."
    )
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
@Operation(
        summary = "Update provider details",
        description = "Updates the details of an existing service provider identified by their unique ID.")
    @PutMapping("/{id}")
    public ResponseEntity<ProviderResponse> updateProvider(
            @PathVariable Long id,
            @Valid @RequestBody ProviderRequest request) {

        return ResponseEntity.ok(
                providerService.updateProvider(id, request)
        );
    }
@Operation(
        summary = "Delete provider",
        description = "Deletes an existing service provider identified by their unique ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvider(
            @PathVariable Long id) {

        providerService.deleteProvider(id);

        return ResponseEntity.noContent().build();
    }
    @Operation(
            summary = "Update provider availability",
            description = "Updates the availability status of an existing service provider identified by their unique ID."
    )
    @PatchMapping("/{id}/availability")
    public ResponseEntity<ProviderResponse> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityUpdateRequest request) {

        return ResponseEntity.ok(
                providerService.updateAvailability(id, request)
        );
    }
    @Operation(
            summary = "Add skill to provider",
            description = "Adds a new skill to an existing service provider identified by their unique ID."
    )
    @PostMapping("/{providerId}/skills")
    public ResponseEntity<SkillResponse> addSkill(
            @PathVariable Long providerId,
            @Valid @RequestBody SkillRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        providerService.addSkill(
                                providerId,
                                request
                        )
                );
    }
    @Operation(
            summary = "Get skills of provider",
            description = "Retrieves a list of skills associated with an existing service provider identified by their unique ID."
    )
    @GetMapping("/{providerId}/skills")
    public ResponseEntity<List<SkillResponse>> getSkills(
            @PathVariable Long providerId) {

        return ResponseEntity.ok(
                providerService.getSkills(providerId)
        );
    }
    @Operation(
            summary = "Delete skill from provider",
            description = "Deletes a skill from an existing service provider identified by their unique ID."
    )
    @DeleteMapping("/{providerId}/skills/{skillId}")
    public ResponseEntity<Void> deleteSkill(
            @PathVariable Long providerId,
            @PathVariable Long skillId) {

        providerService.deleteSkill(
                providerId,
                skillId
        );

        return ResponseEntity.noContent().build();
    }
    @Operation(
            summary = "Get current provider profile",
            description = "Retrieves the profile associated with the authenticated provider."
    )
    @GetMapping("/me")
    public ResponseEntity<ProviderResponse> getMyProvider(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.valueOf(jwt.getSubject());

        return ResponseEntity.ok(
                providerService.getProviderByUserId(userId)
        );
    }
}
