package com.epam.engagement_system.service;

import com.epam.engagement_system.domain.Appointment;
import com.epam.engagement_system.service.report.ReportGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService Unit Tests")
class ReportServiceTest {

    @Mock
    @Qualifier("pdfReportGenerator")
    private ReportGenerator pdfGenerator;

    @Mock
    @Qualifier("excelReportGenerator")
    private ReportGenerator excelGenerator;

    @Mock
    private AppointmentService appointmentService;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(pdfGenerator, excelGenerator, appointmentService);
    }

    @Nested
    @DisplayName("Generate Report")
    class GenerateReportTests {
        private final LocalDate startDate = LocalDate.of(2025, 9, 1);
        private final LocalDate endDate = LocalDate.of(2025, 9, 30);
        private final List<Appointment> mockAppointments = Collections.emptyList();

        @Test
        @DisplayName("should use pdf generator for the pdf format")
        void generateReport_PdfFormat_ShouldUsePdfGenerator() {
            String format = "pdf";
            ByteArrayInputStream pdfStream = new ByteArrayInputStream("pdf-content".getBytes());
            when(pdfGenerator.generate(mockAppointments, startDate, endDate)).thenReturn(pdfStream);
            when(pdfGenerator.getFilenameExtension()).thenReturn(".pdf");
            when(pdfGenerator.getContentType()).thenReturn("application/pdf");

            ReportService.Report report = reportService.generateReport(format, startDate, endDate);

            assertNotNull(report);
            assertEquals("application/pdf", report.contentType());
            assertTrue(report.fileName().endsWith(".pdf"));
            verify(pdfGenerator).generate(mockAppointments, startDate, endDate);
            verify(excelGenerator, never()).generate(any(), any(), any());
        }

        @Test
        @DisplayName("should use excel generator for excel format")
        void generateReport_ExcelFormat_ShouldUseExcelGenerator() {
            String format = "excel";
            ByteArrayInputStream excelStream = new ByteArrayInputStream("excel-content".getBytes());
            when(excelGenerator.generate(mockAppointments, startDate, endDate)).thenReturn(excelStream);
            when(excelGenerator.getFilenameExtension()).thenReturn(".xlsx");
            when(excelGenerator.getContentType()).thenReturn("application/vnd.ms-excel");

            ReportService.Report report = reportService.generateReport(format, startDate, endDate);

            assertNotNull(report);
            assertEquals("application/vnd.ms-excel", report.contentType());
            assertTrue(report.fileName().endsWith(".xlsx"));
            verify(excelGenerator).generate(mockAppointments, startDate, endDate);
            verify(pdfGenerator, never()).generate(any(), any(), any());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for unknown format")
        void generateReport_UnknownFormat_ShouldThrowException() {
            String format = "csv";

            assertThrows(IllegalArgumentException.class,
                    () -> reportService.generateReport(format, startDate, endDate));
        }
    }
}
