package com.epam.engagement_system.service.report;

import com.epam.engagement_system.domain.Appointment;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component("pdfReportGenerator")
public class PdfReportGenerator implements ReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PdfReportGenerator.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public ByteArrayInputStream generate(List<Appointment> appointments, LocalDate startDate, LocalDate endDate) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate());

            document.add(new Paragraph("Astana Grand Mosque - Report on Appointments")
                    .setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(16));
            document.add(new Paragraph("Period: " + startDate.format(DATE_FORMATTER) + " - " + endDate.format(DATE_FORMATTER))
                    .setTextAlignment(TextAlignment.CENTER).setFontSize(12));
            document.add(new Paragraph("Generated on: " + LocalDateTime.now().format(DATE_TIME_FORMATTER))
                    .setTextAlignment(TextAlignment.CENTER).setFontSize(10).setItalic());

            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 4, 4, 5, 5, 3, 5, 5, 5, 6, 6}));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell(createHeaderCell("ID"));
            table.addHeaderCell(createHeaderCell("Applicant Phone"));
            table.addHeaderCell(createHeaderCell("Ceremony Time"));
            table.addHeaderCell(createHeaderCell("Groom"));
            table.addHeaderCell(createHeaderCell("Bride"));
            table.addHeaderCell(createHeaderCell("Status"));
            table.addHeaderCell(createHeaderCell("Witness 1"));
            table.addHeaderCell(createHeaderCell("Witness 2"));
            table.addHeaderCell(createHeaderCell("Witness 3"));
            table.addHeaderCell(createHeaderCell("Notes"));
            table.addHeaderCell(createHeaderCell("Rejection reason"));

            for (Appointment app : appointments) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(app.getId())).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(app.getApplicant().getPhoneNumber())).setFontSize(8));
                table.addCell(new Cell().add(new Paragraph(app.getTimeSlot().getStartTime().format(DATE_TIME_FORMATTER))).setFontSize(8));
                table.addCell(new Cell().add(new Paragraph(app.getGroomFirstName() + " " + app.getGroomLastName())).setFontSize(8));
                table.addCell(new Cell().add(new Paragraph(app.getBrideFirstName() + " " + app.getBrideLastName())).setFontSize(8));
                table.addCell(new Cell().add(new Paragraph(app.getStatus().name())).setFontSize(8));
                table.addCell(new Cell().add(new Paragraph(app.getWitness1FirstName() + " " + app.getWitness1LastName())).setFontSize(8));
                table.addCell(new Cell().add(new Paragraph(app.getWitness2FirstName() + " " + app.getWitness2LastName())).setFontSize(8));
                String witness3 = app.getWitness3FirstName() != null ? app.getWitness3FirstName() + " " + app.getWitness3LastName() : "";
                table.addCell(new Cell().add(new Paragraph(witness3)).setFontSize(8));
                table.addCell(new Cell().add(new Paragraph(app.getNotes() != null ? app.getNotes() : "")).setFontSize(8));
                table.addCell(new Cell().add(new Paragraph(app.getRejectionReason() != null ? app.getRejectionReason() : "")).setFontSize(8));
            }

            document.add(table);
            document.close();

            logger.info("Successfully generated pdf appointment report from {} to {}", startDate, endDate);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            logger.error("Failure to generate appointment report pdf file: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String getContentType() {
        return "application/pdf";
    }

    @Override
    public String getFilenameExtension() {
        return ".pdf";
    }

    private Cell createHeaderCell(String text) {
        return new Cell().add(new Paragraph(text).setBold().setFontSize(9));
    }
}
