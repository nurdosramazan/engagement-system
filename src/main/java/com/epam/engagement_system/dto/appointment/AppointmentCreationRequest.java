package com.epam.engagement_system.dto.appointment;

import com.epam.engagement_system.domain.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AppointmentCreationRequest(
        @NotNull(message = "Time slot ID cannot be null")
        Long timeSlotId,

        @NotBlank(message = "Spouse's first name is required")
        @Size(min = 2, max = 20, message = "Spouse's first name must be between 2 and 20 characters")
        String spouseFirstName,

        @NotBlank(message = "Spouse's last name is required")
        @Size(min = 2, max = 20, message = "Spouse's last name must be between 2 and 20 characters")
        String spouseLastName,

        @NotEmpty(message = "At least two witnesses are required")
        @Valid
        List<WitnessInfo> witnesses,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        String notes
) {

    public record WitnessInfo(
            @NotBlank(message = "Witness first name cannot be blank")
            @Size(min = 2, max = 20, message = "Witness first name must be between 2 and 20 characters")
            String firstName,
            @NotBlank(message = "Witness last name cannot be blank")
            @Size(min = 2, max = 20, message = "Witness last name must be between 2 and 20 characters")
            String lastName,
            @NotNull(message = "Witness gender cannot be null") Gender gender
    ) {}
}
