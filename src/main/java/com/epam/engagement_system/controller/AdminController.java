package com.epam.engagement_system.controller;

import com.epam.engagement_system.dto.ApiResponse;
import com.epam.engagement_system.dto.admin.SlotGenerationRequest;
import com.epam.engagement_system.service.TimeSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final TimeSlotService timeSlotService;
    @PostMapping("/time-slots/generate")
    public ResponseEntity<ApiResponse<Object>> generateTimeSlots(@Valid @RequestBody SlotGenerationRequest request) {
        String message = timeSlotService.generateSlotsForMonth(request.year(), request.month());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, message, null));
    }
}
