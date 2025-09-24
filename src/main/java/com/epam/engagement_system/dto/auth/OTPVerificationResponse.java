package com.epam.engagement_system.dto.auth;

public record OTPVerificationResponse(
        String accessToken,
        String tokenType
) {}
