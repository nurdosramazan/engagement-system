package com.epam.engagement_system.service;

import com.epam.engagement_system.domain.Appointment;
import com.epam.engagement_system.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderService {
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);
    private static final String address = "Mangilik el STREET, 65";

    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void sendAppointmentReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> appointmentsToRemind =
                appointmentRepository.findAppointmentsForReminder(now, now.plusHours(24));
        if (appointmentsToRemind.isEmpty()) {
            return;
        }

        appointmentsToRemind.forEach(appointment -> {
            String message = String.format(
                    "Reminder: Your engagement will take place on %s at the address %s",
                    appointment.getTimeSlot().getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                    address
            );

            notificationService.createAndSendNotification(appointment.getApplicant(), message);
            appointment.setReminderSent(true);
            logger.info("Request was sent for reminder for appointment {}", appointment.getId());
        });
    }
}
