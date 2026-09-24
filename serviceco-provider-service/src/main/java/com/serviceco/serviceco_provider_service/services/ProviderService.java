package com.serviceco.serviceco_provider_service.services;

import com.serviceco.serviceco_provider_service.model.dto.AvailabilityUpdateRequest;
import com.serviceco.serviceco_provider_service.model.dto.ProviderRequest;
import com.serviceco.serviceco_provider_service.model.dto.ProviderResponse;
import com.serviceco.serviceco_provider_service.model.dto.ProviderSearchResponse;
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
}
