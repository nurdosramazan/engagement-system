package com.epam.engagement_system.dto.appointment;

import java.time.Instant;
import java.time.LocalDateTime;

public record AppointmentInformationResponse(
        Long id,
        String status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String applicantPhoneNumber,
        int historyCount,
        String groomFirstName,
        String groomLastName,
        String brideFirstName,
        String brideLastName,
        String witness1FirstName,
        String witness1LastName,
        String witness2FirstName,
        String witness2LastName,
        String witness3FirstName,
        String witness3LastName,
        String notes,
        Instant createdAt,
        String documentPath,
        String rejectionReason
) { }
