package com.epam.engagement_system.controller;

import com.epam.engagement_system.dto.ApiResponse;
import com.epam.engagement_system.dto.appointment.TimeSlotInformationResponse;
import com.epam.engagement_system.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    @GetMapping("/available-slots")
    public ResponseEntity<ApiResponse<List<TimeSlotInformationResponse>>> getAvailableTimeSlots(@RequestParam int year,
                                                                                                @RequestParam int month) {
        List<TimeSlotInformationResponse> availableSlots = appointmentService.getAvailableTimeSlotsDto(year, month);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Available slots fetched successfully", availableSlots));
    }
}
