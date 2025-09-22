package com.epam.engagement_system.dto.user;

import com.epam.engagement_system.domain.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "First name cannot be blank")
        @Size(min = 2, max = 20, message = "First name must be between 2 and 20 characters")
        String firstName,

        @NotBlank(message = "Last name cannot be blank")
        @Size(min = 2, max = 20, message = "Last name must be between 2 and 20 characters")
        String lastName,

        @NotNull(message = "Gender have to be specified")
        Gender gender)
{}
