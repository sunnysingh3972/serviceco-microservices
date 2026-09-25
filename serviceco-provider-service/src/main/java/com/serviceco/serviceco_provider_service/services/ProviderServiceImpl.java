package com.serviceco.serviceco_provider_service.services;

import com.serviceco.serviceco_provider_service.exception.ProviderNotFoundException;
import com.serviceco.serviceco_provider_service.mapper.ProviderMapper;
import com.serviceco.serviceco_provider_service.model.dto.*;
import com.serviceco.serviceco_provider_service.model.entity.Provider;
import com.serviceco.serviceco_provider_service.model.entity.ProviderSkill;
import com.serviceco.serviceco_provider_service.repository.ProviderRepository;
import com.serviceco.serviceco_provider_service.repository.ProviderSkillRepository;
import com.serviceco.serviceco_provider_service.utility.ProviderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProviderServiceImpl implements ProviderService{
    private final ProviderRepository providerRepository;
    private final ProviderMapper providerMapper;
    private  final ProviderSkillRepository providerSkillRepository;
    @Override
    public ProviderResponse createProvider(ProviderRequest request) {
        Provider provider=providerMapper.toEntity(request);
        Provider savedProvider = providerRepository.save(provider);
        return providerMapper.toResponse(savedProvider);
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderResponse getProvider(Long id) {
       Provider provider= providerRepository.findById(id).orElseThrow(()->new ProviderNotFoundException("Provider not find by "+id));
        return providerMapper.toResponse(provider);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProviderResponse> getAllProviders(String location, Pageable pageable) {
        Page<Provider> providers=null;
        if(location==null||location.isBlank()){
            providers=providerRepository.findAll(pageable);
        }else{
            providers=providerRepository.findByLocationIgnoreCase(location,pageable);
        }
        return providers.map(providerMapper::toResponse);

    }

    @Override
    public ProviderResponse updateProvider(Long id, ProviderRequest request) {
        Provider provider =
                providerRepository.findById(id)
                        .orElseThrow(() ->
                                new ProviderNotFoundException(
                                        "Provider not found with id: " + id
                                ));

        provider.setName(request.name());
        provider.setEmail(request.email());
        provider.setPhoneNumber(request.phoneNumber());
        provider.setExperience(request.experience());
        provider.setLocation(request.location());
        provider.setHourlyRate(request.hourlyRate());

        return providerMapper.toResponse(provider);
    }

    @Override
    public void deleteProvider(Long id) {
        Provider provider =
                providerRepository.findById(id)
                        .orElseThrow(() ->
                                new ProviderNotFoundException(
                                        "Provider not found with id: " + id
                                ));

        providerRepository.delete(provider);
    }



    @Override
    @Transactional(readOnly = true)
    public Page<ProviderSearchResponse> searchProviders(
            String location,
            String skill,
            ProviderStatus status,
            Pageable pageable) {

        return providerRepository.searchProviders(
                location,
                skill,
                status,
                pageable
        );
    }

    @Override
    public ProviderResponse updateAvailability(Long id, AvailabilityUpdateRequest request) {
        Provider provider =
                providerRepository.findById(id)
                        .orElseThrow(() ->
                                new ProviderNotFoundException(
                                        "Provider not found with id: " + id
                                ));

        provider.setStatus(request.status());

        return providerMapper.toResponse(provider);
    }

    @Override
    @Transactional
    public SkillResponse addSkill(
            Long providerId,
            SkillRequest request) {

        providerRepository.findById(providerId)
                .orElseThrow(() ->
                        new ProviderNotFoundException(
                                "Provider not found with id: " + providerId
                        ));

        boolean alreadyExists =
                providerSkillRepository
                        .existsByProviderIdAndSkillNameIgnoreCase(
                                providerId,
                                request.skillName()
                        );

        if (alreadyExists) {
            throw new IllegalArgumentException(
                    "Skill already exists for this provider"
            );
        }

        ProviderSkill skill = ProviderSkill.builder()
                .skillName(request.skillName().trim().toUpperCase())
                .provider(
                        providerRepository
                                .findById(providerId)
                                .orElseThrow()
                )
                .build();

        ProviderSkill savedSkill =
                providerSkillRepository.save(skill);

        return new SkillResponse(
                savedSkill.getId(),
                savedSkill.getSkillName()
        );
    }

    @Override
    @Transactional
    public List<SkillResponse> getSkills(Long providerId) {
        providerRepository.findById(providerId)
                .orElseThrow(() ->
                        new ProviderNotFoundException(
                                "Provider not found with id: " + providerId
                        ));

        return providerSkillRepository
                .findByProviderIdOrderBySkillNameAsc(providerId)
                .stream()
                .map(skill ->
                        new SkillResponse(
                                skill.getId(),
                                skill.getSkillName()
                        )
                )
                .toList();
    }

    @Override
    @Transactional
    public void deleteSkill(Long providerId, Long skillId) {
        ProviderSkill skill =
                providerSkillRepository
                        .findByIdAndProviderId(
                                skillId,
                                providerId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Skill not found for this provider"
                                ));

        providerSkillRepository.delete(skill);
    }
}
