package com.epam.engagement_system.service;

import com.epam.engagement_system.domain.TimeSlot;
import com.epam.engagement_system.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TimeSlotService {
    private final TimeSlotRepository timeSlotRepository;

    private static final LocalTime OPENING_TIME = LocalTime.of(9, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(18, 0);
    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_END = LocalTime.of(13, 0);
    private static final LocalTime FRIDAY_LUNCH_START = LocalTime.of(12, 30);
    private static final LocalTime FRIDAY_LUNCH_END = LocalTime.of(14, 0);
    private static final long SLOT_DURATION_MINUTES = 30;

    private static final Logger logger = LoggerFactory.getLogger(TimeSlotService.class);
    @Transactional
    public String generateSlotsForMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        if (yearMonth.isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("Cannot generate time slots for a past month.");
        }

        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        Set<LocalDateTime> existingSlots = timeSlotRepository.findExistingStartTimes(startOfMonth, endOfMonth);

        List<TimeSlot> newSlots = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int day = 1; day <= yearMonth.lengthOfMonth(); ++day) {
            LocalDate currentDate = yearMonth.atDay(day);
            if (currentDate.isBefore(today)) {
                continue;
            }

            LocalDateTime currentTimeSlot = currentDate.atTime(OPENING_TIME);
            LocalDateTime closingTime = currentDate.atTime(CLOSING_TIME);

            while (!currentTimeSlot.plusMinutes(SLOT_DURATION_MINUTES).isAfter(closingTime)) {
                LocalDateTime slotEndTime = currentTimeSlot.plusMinutes(SLOT_DURATION_MINUTES);
                LocalTime start = currentTimeSlot.toLocalTime();
                LocalTime end = slotEndTime.toLocalTime();


                if (isDuringLunchBreak(currentDate.getDayOfWeek(), start, end)) {
                    currentTimeSlot = currentTimeSlot.plusMinutes(SLOT_DURATION_MINUTES);
                    continue;
                }

                if (!existingSlots.contains(currentTimeSlot)) {
                    TimeSlot newSlot = new TimeSlot();
                    newSlot.setStartTime(currentTimeSlot);
                    newSlot.setEndTime(slotEndTime);
                    newSlot.setAvailable(true);
                    newSlots.add(newSlot);
                }
                currentTimeSlot = slotEndTime;
            }
        }
        if (!newSlots.isEmpty()) {
            timeSlotRepository.saveAll(newSlots);
        }

        String successMessage = String.format(
                "Successfully generated %d new time slots for %d-%02d.", newSlots.size(), year, month);
        logger.info(successMessage);
        return successMessage;
    }

    private boolean isDuringLunchBreak(DayOfWeek dayOfWeek, LocalTime start, LocalTime end) {
        if (dayOfWeek == DayOfWeek.FRIDAY) {
            return start.isBefore(FRIDAY_LUNCH_END) && end.isAfter(FRIDAY_LUNCH_START);
        } else {
            return start.isBefore(LUNCH_END) && end.isAfter(LUNCH_START);
        }
    }
}
