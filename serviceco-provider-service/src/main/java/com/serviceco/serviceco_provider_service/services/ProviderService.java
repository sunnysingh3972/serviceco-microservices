package com.serviceco.serviceco_provider_service.services;

import com.serviceco.serviceco_provider_service.model.dto.*;
import com.serviceco.serviceco_provider_service.utility.ProviderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

public interface  ProviderService {

    ProviderResponse createProvider(
            ProviderRequest request);

    ProviderResponse getProvider(Long id);

    Page<ProviderResponse> getAllProviders(
            String location,
            Pageable pageable
    );

    ProviderResponse updateProvider(
            Long id,
            ProviderRequest request
    );

    void deleteProvider(Long id);


    Page<ProviderSearchResponse> searchProviders(
            String location,
            String skill,
            ProviderStatus status,
            Pageable pageable);
    ProviderResponse updateAvailability(
            Long id,
            AvailabilityUpdateRequest request
    );
    SkillResponse addSkill(
            Long providerId,
            SkillRequest request
    );

    List<SkillResponse> getSkills(
            Long providerId
    );

    void deleteSkill(
            Long providerId,
            Long skillId
    );
}
