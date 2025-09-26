package com.epam.engagement_system.service;

import com.epam.engagement_system.domain.TimeSlot;
import com.epam.engagement_system.repository.TimeSlotRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimeSlotService Unit Tests")
class TimeSlotServiceTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @InjectMocks
    private TimeSlotService timeSlotService;

    @Nested
    @DisplayName("Generate time slots")
    class GenerateSlotsTests {

        @Test
        @DisplayName("should throw IllegalArgumentException for a past month request")
        void generateSlotsForMonth_PastMonth_ShouldThrowException() {
            YearMonth pastMonth = YearMonth.now().minusMonths(1);
            assertThrows(IllegalArgumentException.class,
                    () -> timeSlotService.generateSlotsForMonth(pastMonth.getYear(), pastMonth.getMonthValue()));
        }

        @Test
        @DisplayName("should generate new slots for a future month")
        void generateSlotsForMonth_FutureMonth_ShouldGenerateSlots() {
            YearMonth futureMonth = YearMonth.now().plusMonths(1);

            when(timeSlotRepository.findExistingStartTimes(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptySet());

            String message = timeSlotService.generateSlotsForMonth(futureMonth.getYear(), futureMonth.getMonthValue());

            ArgumentCaptor<List<TimeSlot>> captor = ArgumentCaptor.forClass(List.class);
            verify(timeSlotRepository).saveAll(captor.capture());

            List<TimeSlot> savedSlots = captor.getValue();
            assertFalse(savedSlots.isEmpty());
            LocalDateTime firstSlotStart = futureMonth.atDay(1).atTime(9, 0);
            if (!savedSlots.isEmpty()) {
                assertEquals(firstSlotStart, savedSlots.getFirst().getStartTime());
            }
            assertTrue(message.contains("Successfully generated"));
            assertFalse(message.contains("0 new time slots"));
        }

        @Test
        @DisplayName("should not generate slots for days of this month before today")
        void generateSlotsForMonth_CurrentMonth_ShouldSkipPastDays() {
            YearMonth currentMonth = YearMonth.now();
            if (java.time.LocalDate.now().getDayOfMonth() > 1) {
                when(timeSlotRepository.findExistingStartTimes(any(), any())).thenReturn(Collections.emptySet());

                timeSlotService.generateSlotsForMonth(currentMonth.getYear(), currentMonth.getMonthValue());

                ArgumentCaptor<List<TimeSlot>> captor = ArgumentCaptor.forClass(List.class);
                verify(timeSlotRepository).saveAll(captor.capture());

                List<TimeSlot> savedSlots = captor.getValue();
                assertFalse(savedSlots.isEmpty());

                assertTrue(savedSlots.getFirst().getStartTime().toLocalDate().isAfter(currentMonth.atDay(1).minusDays(1)));
            }
        }
    }
}
