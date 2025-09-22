package com.epam.engagement_system.controller;

import com.epam.engagement_system.dto.ApiResponse;
import com.epam.engagement_system.dto.appointment.AppointmentCreationRequest;
import com.epam.engagement_system.dto.appointment.AppointmentCreationResponse;
import com.epam.engagement_system.dto.appointment.AppointmentInformationResponse;
import com.epam.engagement_system.dto.appointment.TimeSlotInformationResponse;
import com.epam.engagement_system.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/my-appointments")
    public ResponseEntity<ApiResponse<List<AppointmentInformationResponse>>> getMyAppointments(Object ignored) {
        List<AppointmentInformationResponse> myAppointments = appointmentService.findAppointmentsByUserId(1L);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "User appointments fetched successfully", myAppointments));
    }

    @GetMapping("/available-slots")
    public ResponseEntity<ApiResponse<List<TimeSlotInformationResponse>>> getAvailableTimeSlots(@RequestParam int year,
                                                                                                @RequestParam int month) {
        List<TimeSlotInformationResponse> availableSlots = appointmentService.getAvailableTimeSlotsDto(year, month);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Available slots fetched successfully", availableSlots));
    }

//    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentCreationResponse>> createAppointment(@Valid @RequestBody AppointmentCreationRequest request,
                                                                                      //@RequestPart("file") MultipartFile file,
                                                                                      Object ignored) {
        AppointmentCreationResponse response = appointmentService.createAppointment(request, null, 1L);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Appointment request submitted successfully.", response));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Object>> cancelMyAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Your appointment has been successfully cancelled.", null));
    }

}
