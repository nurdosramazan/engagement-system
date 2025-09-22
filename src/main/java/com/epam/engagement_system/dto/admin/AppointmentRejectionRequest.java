package com.epam.engagement_system.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppointmentRejectionRequest (
    @NotBlank(message = "Rejection reason cannot be blank")
    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    String reason
) {}
