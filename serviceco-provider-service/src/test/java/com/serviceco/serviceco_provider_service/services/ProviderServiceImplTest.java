package com.serviceco.serviceco_provider_service.services;

import com.serviceco.serviceco_provider_service.exception.DuplicateSkillException;
import com.serviceco.serviceco_provider_service.exception.ProviderNotFoundException;
import com.serviceco.serviceco_provider_service.mapper.ProviderMapper;

import com.serviceco.serviceco_provider_service.model.dto.ProviderRequest;
import com.serviceco.serviceco_provider_service.model.dto.ProviderResponse;
import com.serviceco.serviceco_provider_service.model.dto.SkillRequest;
import com.serviceco.serviceco_provider_service.model.dto.SkillResponse;
import com.serviceco.serviceco_provider_service.model.entity.Provider;
import com.serviceco.serviceco_provider_service.model.entity.ProviderSkill;
import com.serviceco.serviceco_provider_service.repository.ProviderRepository;
import com.serviceco.serviceco_provider_service.repository.ProviderSkillRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProviderServiceImplTest {

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private ProviderSkillRepository providerSkillRepository;

    @Mock
    private ProviderMapper providerMapper;

    @InjectMocks
    private ProviderServiceImpl providerService;
    @Test
    void shouldCreateProviderSuccessfully() {

        // Arrange
        ProviderRequest request = new ProviderRequest(
                "Rahul Sharma",
                "rahul@gmail.com",
                "9876543210",
                5,
                "Noida",
                new BigDecimal("500"),
                List.of("BABYSITTING")
        );

        Provider provider = Provider.builder()
                .id(1L)
                .name("Rahul Sharma")
                .email("rahul@gmail.com")
                .phoneNumber("9876543210")
                .experience(5)
                .location("Noida")
                .hourlyRate(new BigDecimal("500"))
                .rating(0.0)
                .build();

        ProviderResponse response = new ProviderResponse(
                1L,
                "Rahul Sharma",
                "9876543210",
                "Noida",
                5,
                "Noida",
                new BigDecimal("500"),
                0.0,
                null,
                List.of("BABYSITTING")
        );

        when(providerMapper.toEntity(request))
                .thenReturn(provider);

        when(providerRepository.save(provider))
                .thenReturn(provider);

        when(providerMapper.toResponse(provider))
                .thenReturn(response);

        // Act
        ProviderResponse result =
                providerService.createProvider(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Rahul Sharma", result.name());
        assertEquals("Noida", result.location());
        assertEquals(
                new BigDecimal("500"),
                result.hourlyRate()
        );

        verify(providerMapper)
                .toEntity(request);

        verify(providerRepository)
                .save(provider);

        verify(providerMapper)
                .toResponse(provider);
    }
    @Test
    void shouldThrowExceptionWhenProviderDoesNotExist() {

        when(providerRepository.findByIdWithSkills(999L))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                ProviderNotFoundException.class,
                () -> providerService.getProvider(999L)
        );

        verify(providerRepository)
                .findByIdWithSkills(999L);

        verifyNoInteractions(providerMapper);
    }
    @Test
    void shouldAddSkillSuccessfully() {

        Long providerId = 1L;

        Provider provider = Provider.builder()
                .id(providerId)
                .name("Rahul Sharma")
                .build();

        SkillRequest request =
                new SkillRequest("cooking");

        ProviderSkill savedSkill = ProviderSkill.builder()
                .id(10L)
                .skillName("COOKING")
                .provider(provider)
                .build();

        when(providerRepository.findById(providerId))
                .thenReturn(Optional.of(provider));

        when(providerSkillRepository
                .existsByProviderIdAndSkillNameIgnoreCase(
                        providerId,
                        request.skillName()
                ))
                .thenReturn(false);

        when(providerSkillRepository.save(any(ProviderSkill.class)))
                .thenReturn(savedSkill);

        SkillResponse result =
                providerService.addSkill(
                        providerId,
                        request
                );

        assertNotNull(result);
        assertEquals(10L, result.id());
        assertEquals("COOKING", result.skillName());

        verify(providerRepository)
                .findById(providerId);

        verify(providerSkillRepository)
                .existsByProviderIdAndSkillNameIgnoreCase(
                        providerId,
                        request.skillName()
                );

        verify(providerSkillRepository)
                .save(any(ProviderSkill.class));
    }
    @Test
    void shouldThrowExceptionWhenSkillAlreadyExists() {

        Long providerId = 1L;

        Provider provider = Provider.builder()
                .id(providerId)
                .name("Rahul Sharma")
                .build();

        SkillRequest request =
                new SkillRequest("BABYSITTING");

        when(providerRepository.findById(providerId))
                .thenReturn(Optional.of(provider));

        when(providerSkillRepository
                .existsByProviderIdAndSkillNameIgnoreCase(
                        providerId,
                        request.skillName()
                ))
                .thenReturn(true);

        assertThrows(
                DuplicateSkillException.class,
                () -> providerService.addSkill(
                        providerId,
                        request
                )
        );

        verify(providerRepository)
                .findById(providerId);

        verify(providerSkillRepository)
                .existsByProviderIdAndSkillNameIgnoreCase(
                        providerId,
                        request.skillName()
                );

        verify(providerSkillRepository, never())
                .save(any(ProviderSkill.class));
    }
}