package com.epam.engagement_system.dto.appointment;

import com.epam.engagement_system.domain.TimeSlot;

import java.time.LocalDateTime;

public record TimeSlotInformationResponse(Long id, LocalDateTime startTime, LocalDateTime endTime) {}
