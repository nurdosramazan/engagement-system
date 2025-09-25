package com.epam.engagement_system.service;

import com.epam.engagement_system.domain.ApplicationUser;
import com.epam.engagement_system.domain.Appointment;
import com.epam.engagement_system.domain.TimeSlot;
import com.epam.engagement_system.domain.enums.AppointmentStatus;
import com.epam.engagement_system.domain.enums.Gender;
import com.epam.engagement_system.dto.appointment.AppointmentCreationRequest;
import com.epam.engagement_system.dto.appointment.AppointmentCreationResponse;
import com.epam.engagement_system.dto.appointment.AppointmentInformationResponse;
import com.epam.engagement_system.exception.appointment.IllegalAppointmentOperationException;
import com.epam.engagement_system.dto.appointment.TimeSlotInformationResponse;
import com.epam.engagement_system.exception.ResourceNotFoundException;
import com.epam.engagement_system.exception.appointment.ExistingPendingAppointmentException;
import com.epam.engagement_system.exception.appointment.InvalidWitnessException;
import com.epam.engagement_system.exception.appointment.TimeSlotNotAvailableException;
import com.epam.engagement_system.exception.storage.StorageException;
import com.epam.engagement_system.exception.user.ProfileIncompleteException;
import com.epam.engagement_system.repository.AppointmentRepository;
import com.epam.engagement_system.repository.TimeSlotRepository;
import com.epam.engagement_system.repository.UserRepository;
import com.epam.engagement_system.util.AppointmentUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    @Transactional(readOnly = true)
    public List<AppointmentInformationResponse> findByUserId(Long userId) {
        List<Appointment> appointments = appointmentRepository.findByApplicantId(userId);
        int historyCount = appointments.size();

        return appointments.stream()
                .map(appointment -> AppointmentUtil.mapToAppointmentInformationDto(appointment, historyCount))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentInformationResponse> findByStatus(AppointmentStatus status) {
        List<Appointment> appointments = appointmentRepository.findByStatusWithDetails(status);
        int historyCount = appointments.size();

        return appointments.stream()
                .map(appointment -> AppointmentUtil.mapToAppointmentInformationDto(appointment, historyCount))
                .collect(Collectors.toList());
    }

    public List<TimeSlotInformationResponse> getAvailableTimeSlots(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        return timeSlotRepository
                .findByIsAvailableTrueAndStartTimeBetweenOrderByStartTimeAsc(startOfMonth, endOfMonth)
                .stream()
                .map(timeSlot -> new TimeSlotInformationResponse(
                        timeSlot.getId(), timeSlot.getStartTime(), timeSlot.getEndTime()))
                .toList();
    }

    @Transactional
    public AppointmentCreationResponse createAppointment(AppointmentCreationRequest request, MultipartFile file, Long applicantId) {
        validateAbleToCreate(applicantId);
        validateWitnesses(request.witnesses());
        validateFileNotEmpty(file);

        ApplicationUser applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + applicantId));
        validateProfileNotEmpty(applicant);

        TimeSlot timeSlot = timeSlotRepository.findByIdAndIsAvailableTrue(request.timeSlotId())
                .orElseThrow(() -> new TimeSlotNotAvailableException("Time slot is not available or does not exist."));
        timeSlot.setAvailable(false);

        String documentFilename = fileStorageService.store(file);

        Appointment appointment = AppointmentUtil.toAppointment(request, applicant, timeSlot, documentFilename);
        appointmentRepository.save(appointment);

        logger.info("New appointment created by {}.", applicant.getPhoneNumber());

        String userMessage = "Your appointment for " + timeSlot.getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) +
                " was sent for verification. Please wait for reply.";
        notificationService.createAndSendNotification(applicant, userMessage);

        String adminMessage = String.format("New application from %s arrived.", applicant.getPhoneNumber());
        notificationService.createAndSendAdminNotifications(adminMessage);

        return new AppointmentCreationResponse(
                appointment.getId(),
                appointment.getStatus().name(),
                appointment.getCreatedAt()
        );
    }

    @Transactional
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + appointmentId));

        if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.APPROVED) {
            throw new IllegalAppointmentOperationException("Cannot cancel appointment that is " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);

        TimeSlot timeSlot = appointment.getTimeSlot();
        String formattedDate = "?";
        if (timeSlot != null) {
            timeSlot.setAvailable(true);
            formattedDate = timeSlot.getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }
        appointmentRepository.save(appointment);
        logger.info("Appointment {} was cancelled.", appointmentId);

        String userMessage = String.format("Your appointment for %s has been cancelled.", formattedDate);
        notificationService.createAndSendNotification(appointment.getApplicant(), userMessage);
    }

    @Transactional
    public void approveAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found for ID " + appointmentId));
        validateAppointmentIsPending(appointment.getStatus());

        appointment.setStatus(AppointmentStatus.APPROVED);
        appointmentRepository.save(appointment);
        logger.info("Appointment {} was approved", appointmentId);

        String formattedDate = appointment.getTimeSlot().getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        String userMessage = String.format("Your appointment for %s was approved!", formattedDate);
        notificationService.createAndSendNotification(appointment.getApplicant(), userMessage);
    }

    @Transactional
    public void rejectAppointment(Long appointmentId, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found for ID " + appointmentId));
        validateAppointmentIsPending(appointment.getStatus());

        appointment.setStatus(AppointmentStatus.REJECTED);
        appointment.setRejectionReason(reason);

        TimeSlot timeSlot = appointment.getTimeSlot();
        String formattedDate = "?";
        if (timeSlot != null) {
            timeSlot.setAvailable(true);
            formattedDate = timeSlot.getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }
        appointmentRepository.save(appointment);
        logger.info("Appointment {} was rejected", appointmentId);

        String userMessage = String.format(
                "Unfortunately, your application for %s was rejected for the following reason: %s",
                formattedDate,
                reason
        );
        notificationService.createAndSendNotification(appointment.getApplicant(), userMessage);
    }

    @Transactional
    public void completeAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found for ID " + appointmentId));

        if (appointment.getStatus() != AppointmentStatus.APPROVED) {
            throw new IllegalAppointmentOperationException("Cannot complete appointment that is " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
        logger.info("Appointment {} was completed", appointmentId);

        String formattedDate = appointment.getTimeSlot().getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        String userMessage = String.format("Congratulations! Your appointment for %s was competed successfully!", formattedDate);
        notificationService.createAndSendNotification(appointment.getApplicant(), userMessage);
    }

    @Transactional(readOnly = true)
    public String getAppointmentDocumentPath(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        if (appointment.getDocumentPath() == null || appointment.getDocumentPath().isBlank()) {
            throw new ResourceNotFoundException("No document found for appointment with id: " + appointmentId);
        }

        return appointment.getDocumentPath();
    }

    public List<Appointment> getAppointmentsForReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.of(2025, 9, 1);
            logger.warn("Start date is null, setting to {}", startDate);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
            logger.warn("End date is null, setting to {}", endDate);
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        List<Appointment> appointments = appointmentRepository.findAllWithTimeSlotBetween(
                startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        logger.info("Successfully fetched list of appointments from {} till {}", startDate, endDate);
        return appointments;
    }

    private void validateAbleToCreate(Long applicantId) {
        boolean hasPendingOrApproved = appointmentRepository.existsByApplicantIdAndStatusIn(
                applicantId, List.of(AppointmentStatus.PENDING, AppointmentStatus.APPROVED));

        if (hasPendingOrApproved) {
            throw new ExistingPendingAppointmentException("You already have an appointment. Please wait for it to be processed or cancel it before booking a new one.");
        }
    }

    private void validateWitnesses(List<AppointmentCreationRequest.WitnessInfo> witnesses) {
        long maleCount = witnesses.stream().filter(w -> w.gender() == Gender.MALE).count();
        long femaleCount = witnesses.stream().filter(w -> w.gender() == Gender.FEMALE).count();

        boolean isTwoMales = maleCount == 2 && femaleCount == 0;
        boolean isOneMaleTwoFemales = maleCount == 1 && femaleCount == 2;

        if (!(isTwoMales || isOneMaleTwoFemales)) {
            throw new InvalidWitnessException(
                    "Witness requirements not met: Must be 2 males, or 1 male and 2 females."
            );
        }
    }

    private void validateProfileNotEmpty(ApplicationUser user) {
        if (user.getFirstName() == null || user.getLastName() == null || user.getGender() == null) {
            throw new ProfileIncompleteException("Applicant's first name, last name or gender not provided.");
        }
    }

    private void validateAppointmentIsPending(AppointmentStatus status) {
        if (status != AppointmentStatus.PENDING) {
            throw new IllegalAppointmentOperationException("Cannot approve/reject appointment that is " + status);
        }
    }

    private void validateFileNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("A document file is required for the appointment.");
        }
    }
}
