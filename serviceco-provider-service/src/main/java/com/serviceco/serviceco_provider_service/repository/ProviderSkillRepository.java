package com.serviceco.serviceco_provider_service.repository;

import com.serviceco.serviceco_provider_service.model.entity.ProviderSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProviderSkillRepository extends JpaRepository<ProviderSkill, Long> {
    List<ProviderSkill> findByProviderIdOrderBySkillNameAsc(
            Long providerId
    );

    boolean existsByProviderIdAndSkillNameIgnoreCase(
            Long providerId,
            String skillName
    );

    java.util.Optional<ProviderSkill> findByIdAndProviderId(
            Long skillId,
            Long providerId
    );
}
