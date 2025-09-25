package com.epam.engagement_system.service.report;

import com.epam.engagement_system.domain.Appointment;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

public interface ReportGenerator {
    ByteArrayInputStream generate(List<Appointment> appointments, LocalDate startDate, LocalDate endDate);
    String getContentType();
    String getFilenameExtension();
}
