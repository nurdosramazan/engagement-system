package com.epam.engagement_system.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OTPVerificationRequest(
        @NotBlank(message = "Phone number cannot be blank!")
        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Invalid phone number format")
        String phoneNumber,
        @NotBlank(message = "OTP code cannot be blank")
        @Size(min = 6, max = 6, message = "OTP must be 6 digits")
        String otp
) { }
