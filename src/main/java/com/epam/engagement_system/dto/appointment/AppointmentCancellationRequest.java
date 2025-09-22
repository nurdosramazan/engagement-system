package com.epam.engagement_system.dto.appointment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppointmentCancellationRequest (
    @NotBlank(message = "A reason for cancellation is required.")
    @Size(max = 255, message = "Reason cannot exceed 255 characters.")
    String reason
) {}
