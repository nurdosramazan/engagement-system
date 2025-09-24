package com.epam.engagement_system.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OTPRequest(
        @NotBlank(message = "Phone number cannot be blank!")
        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Invalid phone number format")
        String phoneNumber
) {}
