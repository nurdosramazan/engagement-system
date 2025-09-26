package com.epam.engagement_system.service;

import com.epam.engagement_system.domain.ApplicationUser;
import com.epam.engagement_system.domain.Appointment;
import com.epam.engagement_system.domain.TimeSlot;
import com.epam.engagement_system.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReminderService Unit Tests")
class ReminderServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReminderService reminderService;

    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        ApplicationUser user = new ApplicationUser("+7702123123");
        user.setId(1L);

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStartTime(LocalDateTime.now().plusHours(12));

        testAppointment = new Appointment();
        testAppointment.setId(100L);
        testAppointment.setApplicant(user);
        testAppointment.setTimeSlot(timeSlot);
        testAppointment.setReminderSent(false);
    }

    @Test
    @DisplayName("should send reminders for coming appointments and update their status")
    void sendAppointmentReminders_WithDueAppointments_ShouldSendAndMark() {
        when(appointmentRepository.findAppointmentsForReminder(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(testAppointment));

        reminderService.sendAppointmentReminders();

        verify(notificationService).createAndSendNotification(eq(testAppointment.getApplicant()), anyString());
        assertTrue(testAppointment.isReminderSent());
    }
}
