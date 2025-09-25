package com.epam.engagement_system.dto;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String message,
        boolean isRead,
        Instant createdAt
) {}
