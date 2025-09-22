package com.epam.engagement_system.service;

import com.epam.engagement_system.domain.ApplicationUser;
import com.epam.engagement_system.domain.Appointment;
import com.epam.engagement_system.domain.TimeSlot;
import com.epam.engagement_system.domain.enums.AppointmentStatus;
import com.epam.engagement_system.domain.enums.Gender;
import com.epam.engagement_system.dto.appointment.AppointmentCreationRequest;
import com.epam.engagement_system.dto.appointment.AppointmentCreationResponse;
import com.epam.engagement_system.dto.appointment.AppointmentInformationResponse;
import com.epam.engagement_system.dto.appointment.IllegalAppointmentOperationException;
import com.epam.engagement_system.dto.appointment.TimeSlotInformationResponse;
import com.epam.engagement_system.exception.ResourceNotFoundException;
import com.epam.engagement_system.exception.appointment.ExistingPendingAppointmentException;
import com.epam.engagement_system.exception.appointment.InvalidWitnessException;
import com.epam.engagement_system.exception.appointment.TimeSlotNotAvailableException;
import com.epam.engagement_system.exception.user.ProfileIncompleteException;
import com.epam.engagement_system.repository.AppointmentRepository;
import com.epam.engagement_system.repository.TimeSlotRepository;
import com.epam.engagement_system.repository.UserRepository;
import com.epam.engagement_system.util.AppointmentUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AppointmentInformationResponse> findAppointmentsByUserId(Long userId) {
        List<Appointment> appointments = appointmentRepository.findByApplicantId(userId);
        int historyCount = appointments.size();

        return appointments.stream()
                .map(appointment -> AppointmentUtil.mapToAppointmentInformationDto(appointment, historyCount))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentInformationResponse> findAppointmentsByStatus(AppointmentStatus status) {
        List<Appointment> appointments = appointmentRepository.findByStatusWithDetails(status);
        int historyCount = appointments.size();

        return appointments.stream()
                .map(appointment -> AppointmentUtil.mapToAppointmentInformationDto(appointment, historyCount))
                .collect(Collectors.toList());
    }

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

    @Transactional
    public AppointmentCreationResponse createAppointment(AppointmentCreationRequest request, MultipartFile ignored, Long applicantId) {
        validateAbleToCreate(applicantId);
        validateWitnesses(request.witnesses());

        ApplicationUser applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + applicantId));
        validateProfileNotEmpty(applicant);

        TimeSlot timeSlot = timeSlotRepository.findByIdAndIsAvailableTrue(request.timeSlotId())
                .orElseThrow(() -> new TimeSlotNotAvailableException("Time slot is not available or does not exist."));
        timeSlot.setAvailable(false);

        Appointment appointment = AppointmentUtil.toAppointment(request, applicant, timeSlot);
        appointmentRepository.save(appointment);

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
        if (timeSlot != null) {
            timeSlot.setAvailable(true);
        }
        appointmentRepository.save(appointment);
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
}
