package com.serviceco.serviceco_provider_service.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SkillRequest(@NotBlank(message = "skill name is required") @Size(max = 50 , message = "skill name cannot exceed 50 character") String skillName) {
}
