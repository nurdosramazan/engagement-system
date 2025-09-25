package com.epam.engagement_system.service.report;

import com.epam.engagement_system.domain.Appointment;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component("excelReportGenerator")
public class ExcelReportGenerator implements ReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ExcelReportGenerator.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public ByteArrayInputStream generate(List<Appointment> appointments, LocalDate startDate, LocalDate endDate) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Appointments");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);

            Row generatedRow = sheet.createRow(0);
            generatedRow.createCell(0).setCellValue("Period: ");
            generatedRow.createCell(1).setCellValue(startDate.format(DATE_FORMATTER) + " - " + endDate.format(DATE_FORMATTER));
            generatedRow.createCell(2).setCellValue("Generated On:");
            generatedRow.createCell(3).setCellValue(LocalDateTime.now().format(DATE_TIME_FORMATTER));

            String[] columns = {"ID", "Applicant Phone", "Ceremony Time", "Groom", "Bride", "Status", "Witness 1", "Witness 2", "Witness 3", "Notes", "Rejection Reason"};
            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 3;
            for (Appointment app : appointments) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(app.getId());
                row.createCell(1).setCellValue(app.getApplicant().getPhoneNumber());
                row.createCell(2).setCellValue(app.getTimeSlot().getStartTime().format(DATE_TIME_FORMATTER));
                row.createCell(3).setCellValue(app.getGroomFirstName() + " " + app.getGroomLastName());
                row.createCell(4).setCellValue(app.getBrideFirstName() + " " + app.getBrideLastName());
                row.createCell(5).setCellValue(app.getStatus().name());
                row.createCell(6).setCellValue(app.getWitness1FirstName() + " " + app.getWitness1LastName());
                row.createCell(7).setCellValue(app.getWitness2FirstName() + " " + app.getWitness2LastName());
                row.createCell(8).setCellValue(app.getWitness3FirstName() != null ? app.getWitness3FirstName() + " " + app.getWitness3LastName() : "");
                row.createCell(9).setCellValue(app.getNotes() != null ? app.getNotes() : "");
                row.createCell(10).setCellValue(app.getRejectionReason() != null ? app.getRejectionReason() : "");

                for(int i = 0; i < columns.length; i++) {
                    if(row.getCell(i) != null) {
                        row.getCell(i).setCellStyle(wrapStyle);
                    }
                }
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);

            logger.info("Successfully generated excel appointment report from {} to {}", startDate, endDate);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            logger.error("Failure to generate appointment report excel file: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String getContentType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    @Override
    public String getFilenameExtension() {
        return ".xlsx";
    }
}
