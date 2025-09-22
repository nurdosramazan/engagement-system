package com.epam.engagement_system.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SlotGenerationRequest (
        @NotNull(message = "Year not provided")
        @Min(value = 2024, message = "Invalid year provided")
        int year,

        @NotNull(message = "Month not provided")
        @Min(value = 1, message = "Month cannot be less than 1")
        @Max(value = 12, message = "Month cannot be greater than 12")
        int month
) {}
