package com.epam.engagement_system.service;

import com.epam.engagement_system.dto.appointment.TimeSlotInformationResponse;
import com.epam.engagement_system.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final TimeSlotRepository timeSlotRepository;
    public List<TimeSlotInformationResponse> getAvailableTimeSlotsDto(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        return timeSlotRepository
                .findByIsAvailableTrueAndStartTimeBetweenOrderByStartTimeAsc(startOfMonth, endOfMonth)
                .stream()
                .map(TimeSlotInformationResponse::mapToDto)
                .toList();
    }
}
