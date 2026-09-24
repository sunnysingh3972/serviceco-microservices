package com.serviceco.serviceco_provider_service.mapper;

import com.serviceco.serviceco_provider_service.model.dto.ProviderRequest;
import com.serviceco.serviceco_provider_service.model.dto.ProviderResponse;
import com.serviceco.serviceco_provider_service.model.entity.Provider;
import com.serviceco.serviceco_provider_service.model.entity.ProviderSkill;
import com.serviceco.serviceco_provider_service.utility.ProviderStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProviderMapper {
    public Provider toEntity(ProviderRequest request) {

        Provider provider=Provider.builder()
                .name(request.name())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .experience(request.experience())
                .location(request.location())
                .hourlyRate(request.hourlyRate())
                .rating(0.0)
                .status(ProviderStatus.AVAILABLE)
                .build();


        List<ProviderSkill> skills =
                request.skills()
                        .stream()
                        .map(skillName ->
                                ProviderSkill.builder()
                                        .skillName(skillName)
                                        .provider(provider)
                                        .build()
                        )
                        .toList();

        provider.setSkills(skills);
        return provider;
    }

    public ProviderResponse toResponse(Provider provider){
        List<String> skills =
                provider.getSkills()
                        .stream()
                        .map(ProviderSkill::getSkillName)
                        .toList();
        return new ProviderResponse(
                provider.getId(),
                provider.getName(),
                provider.getEmail(),
                provider.getPhoneNumber(),
                provider.getExperience(),
                provider.getLocation(),
                provider.getHourlyRate(),
                provider.getRating(),
                provider.getStatus(),
                skills
        );
    }
}
