package com.epam.engagement_system.controller;

import com.epam.engagement_system.dto.ApiResponse;
import com.epam.engagement_system.dto.appointment.AppointmentCreationRequest;
import com.epam.engagement_system.dto.appointment.AppointmentCreationResponse;
import com.epam.engagement_system.dto.appointment.AppointmentInformationResponse;
import com.epam.engagement_system.dto.appointment.TimeSlotInformationResponse;
import com.epam.engagement_system.security.CurrentUser;
import com.epam.engagement_system.security.UserPrincipal;
import com.epam.engagement_system.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/my-appointments")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentInformationResponse>>> getMyAppointments(@CurrentUser UserPrincipal userPrincipal) {
        List<AppointmentInformationResponse> myAppointments = appointmentService.findAppointmentsByUserId(userPrincipal.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "User appointments fetched successfully", myAppointments));
    }

    @GetMapping("/available-slots")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<TimeSlotInformationResponse>>> getAvailableTimeSlots(@RequestParam int year,
                                                                                                @RequestParam int month) {
        List<TimeSlotInformationResponse> availableSlots = appointmentService.getAvailableTimeSlotsDto(year, month);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Available slots fetched successfully", availableSlots));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ApiResponse<AppointmentCreationResponse>> createAppointment(@Valid @RequestPart("request") AppointmentCreationRequest request,
                                                                                      @RequestPart("file") MultipartFile file,
                                                                                      @CurrentUser UserPrincipal userPrincipal) {
        AppointmentCreationResponse response = appointmentService.createAppointment(request, file, userPrincipal.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Appointment request submitted successfully.", response));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("@checkPermission.isAppointmentOwner(principal, #id)")
    public ResponseEntity<ApiResponse<Object>> cancelMyAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Your appointment has been successfully cancelled.", null));
    }

}
