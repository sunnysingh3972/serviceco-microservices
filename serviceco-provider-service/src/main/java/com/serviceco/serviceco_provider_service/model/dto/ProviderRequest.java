package com.serviceco.serviceco_provider_service.model.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record ProviderRequest(@NotBlank(message = "name is required") @Size(min=2,max =30) String name, @NotBlank(message = "Email is required")
@Email(message = "Invalid email format") String email, @NotBlank(message = "phone is required") @Pattern(regexp = "^[0-9]{10}$",message = "Phone must contain 10 digits") String phoneNumber, @NotNull(message = "Experience is required")
@Min(value = 0, message = "Experience cannot be negative") Integer experience, @NotBlank(message = "Location is required")String location ,
                              @NotNull(message = "Hourly rate is required")
                              @DecimalMin(
                                      value = "1.0",
                                      message = "Hourly rate must be greater than 0"
                              )  BigDecimal hourlyRate,@NotEmpty(message = "At least one skill is required")
                              List<@NotBlank(message = "Skill cannot be blank") String> skills) {
}
