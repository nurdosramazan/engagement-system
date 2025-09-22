package com.epam.engagement_system.dto.appointment;

import java.time.Instant;

public record AppointmentCreationResponse(Long id, String status, Instant createdAt) {}
