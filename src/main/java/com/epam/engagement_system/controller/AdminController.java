package com.epam.engagement_system.controller;

import com.epam.engagement_system.domain.enums.AppointmentStatus;
import com.epam.engagement_system.dto.ApiResponse;
import com.epam.engagement_system.dto.admin.AppointmentRejectionRequest;
import com.epam.engagement_system.dto.admin.SlotGenerationRequest;
import com.epam.engagement_system.dto.appointment.AppointmentInformationResponse;
import com.epam.engagement_system.service.AppointmentService;
import com.epam.engagement_system.service.TimeSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final TimeSlotService timeSlotService;
    private final AppointmentService appointmentService;
    @PostMapping("/time-slots/generate")
    public ResponseEntity<ApiResponse<Object>> generateTimeSlots(@Valid @RequestBody SlotGenerationRequest request) {
        String message = timeSlotService.generateSlotsForMonth(request.year(), request.month());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, message, null));
    }

    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<List<AppointmentInformationResponse>>> getAppointmentByStatus(
            @RequestParam(defaultValue = "PENDING") AppointmentStatus status)
    {
        List<AppointmentInformationResponse> appointments = appointmentService.findAppointmentsByStatus(status);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Appointments fetched successfully", appointments));
    }

    @PostMapping("/appointments/{id}/approve")
    public ResponseEntity<ApiResponse<Object>> approveAppointment(@PathVariable Long id) {
        appointmentService.approveAppointment(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Appointment approved successfully", null));
    }

    @PostMapping("/appointments/{id}/reject")
    public ResponseEntity<ApiResponse<Object>> rejectAppointment(@PathVariable Long id,
                                                                 @Valid @RequestBody AppointmentRejectionRequest request) {
        appointmentService.rejectAppointment(id, request.reason());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Appointment rejected successfully", null));
    }

    @PostMapping("/appointments/{id}/complete")
    public ResponseEntity<ApiResponse<Object>> completeAppointment(@PathVariable Long id) {
        appointmentService.completeAppointment(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Appointment marked as completed successfully.", null));
    }

    @PostMapping("/appointments/{id}/cancel")
    public ResponseEntity<ApiResponse<Object>> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Appointment cancelled successfully by admin.", null));
    }
}
