package com.epam.engagement_system.service;

import com.epam.engagement_system.domain.Appointment;
import com.epam.engagement_system.service.report.ReportGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {
    private final ReportGenerator pdfGenerator;
    private final ReportGenerator excelGenerator;
    private final AppointmentService appointmentService;

    public ReportService(@Qualifier("pdfReportGenerator") ReportGenerator pdfGenerator,
                         @Qualifier("excelReportGenerator") ReportGenerator excelGenerator,
                         AppointmentService appointmentService) {
        this.pdfGenerator = pdfGenerator;
        this.excelGenerator = excelGenerator;
        this.appointmentService = appointmentService;
    }

    public Report generateReport(String format, LocalDate startDate, LocalDate endDate) {
        ReportGenerator generator = getGenerator(format);
        List<Appointment> appointments = appointmentService.getAppointmentsForReport(startDate, endDate);

        String fileName = String.format("appointments-report_%s_to_%s%s", startDate, endDate, generator.getFilenameExtension());
        ByteArrayInputStream reportFile = generator.generate(appointments, startDate, endDate);
        String contentType = generator.getContentType();

        return new Report(fileName, reportFile, contentType);
    }

    private ReportGenerator getGenerator(String format) {
        if ("excel".equalsIgnoreCase(format)) {
            return excelGenerator;
        }
        if ("pdf".equalsIgnoreCase(format)) {
            return pdfGenerator;
        }
        throw new IllegalArgumentException("Unknown report format: " + format);
    }

    public record Report(String fileName, ByteArrayInputStream stream, String contentType) {}
}
