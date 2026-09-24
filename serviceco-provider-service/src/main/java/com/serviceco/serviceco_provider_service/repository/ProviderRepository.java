package com.serviceco.serviceco_provider_service.repository;

import com.serviceco.serviceco_provider_service.model.dto.ProviderSearchResponse;
import com.serviceco.serviceco_provider_service.model.entity.Provider;
import com.serviceco.serviceco_provider_service.utility.ProviderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProviderRepository extends JpaRepository<Provider,Long> {

    Page<Provider> findByLocationIgnoreCase(String location, Pageable pageable);
    @Query(value = """
            Select DISTINCT new com.serviceco.serviceco_provider_service.model.dto.ProviderSearchResponse(
            p.id,
            p.name,
            p.location,
            p.hourlyRate,
            p.rating,
            p.status)
           from Provider p
           join p.skills s
           where LOWER(p.location) = LOWER(:location)
              and LOWER(s.skillName) = LOWER(:skill)
                and p.status = :status
           """,
    countQuery = """
            Select  count(Distinct p.id)
           from Provider p
           join p.skills s
           where LOWER(p.location) = LOWER(:location)
              and LOWER(s.skillName) = LOWER(:skill)
                and p.status = :status
           """)
    Page<ProviderSearchResponse> searchProviders(
            @Param("location") String location,
            @Param("skill") String skill,
            @Param("status") ProviderStatus status,
            Pageable pageable
    );
}
