package com.epam.engagement_system.service;

import com.epam.engagement_system.domain.ApplicationUser;
import com.epam.engagement_system.domain.Appointment;
import com.epam.engagement_system.domain.TimeSlot;
import com.epam.engagement_system.domain.enums.AppointmentStatus;
import com.epam.engagement_system.domain.enums.Gender;
import com.epam.engagement_system.dto.appointment.AppointmentInformationResponse;
import com.epam.engagement_system.exception.ResourceNotFoundException;
import com.epam.engagement_system.exception.appointment.IllegalAppointmentOperationException;
import com.epam.engagement_system.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService unit tests")
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AppointmentService appointmentService;

    private ApplicationUser testUser;
    private Appointment testAppointment;
    private TimeSlot testTimeSlot;

    @BeforeEach
    void setUp() {
        testUser = new ApplicationUser("+7702123123");
        testUser.setId(1L);
        testUser.setFirstName("Nurdos");
        testUser.setLastName("Ramazan");
        testUser.setGender(Gender.MALE);

        testTimeSlot = new TimeSlot();
        testTimeSlot.setId(1L);
        testTimeSlot.setStartTime(LocalDateTime.of(2025, 9, 25, 9, 0));
        testTimeSlot.setEndTime(LocalDateTime.of(2025, 9, 25, 9, 30));
        testTimeSlot.setAvailable(true);

        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setApplicant(testUser);
        testAppointment.setTimeSlot(testTimeSlot);
        testAppointment.setStatus(AppointmentStatus.PENDING);
        testAppointment.setCreatedAt(Instant.now());
        testAppointment.setGroomFirstName("Nurdos");
        testAppointment.setGroomLastName("Ramazan");
        testAppointment.setBrideFirstName("br-f");
        testAppointment.setBrideLastName("br-l");
        testAppointment.setWitness1FirstName("wit1-f");
        testAppointment.setWitness1LastName("wit1-l");
        testAppointment.setDocumentPath("document.pdf");
    }

    @Nested
    @DisplayName("Finding appointments")
    class FindingAppointmentsTests {

        @Test
        @DisplayName("findByUserId should return all user's appointments")
        void findByUserId_ShouldReturnUserAppointments() {
            when(appointmentRepository.findByApplicantId(1L)).thenReturn(List.of(testAppointment));

            List<AppointmentInformationResponse> result = appointmentService.findByUserId(1L);
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            assertEquals(testAppointment.getId(), result.getFirst().id());
            verify(appointmentRepository).findByApplicantId(1L);
        }

        @Test
        @DisplayName("findByStatus should return appointments with speciifc status")
        void findByStatus_ShouldReturnAppointments() {
            when(appointmentRepository.findByStatusWithDetails(AppointmentStatus.PENDING)).thenReturn(List.of(testAppointment));

            List<AppointmentInformationResponse> result = appointmentService.findByStatus(AppointmentStatus.PENDING);
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            assertEquals(AppointmentStatus.PENDING.name(), result.getFirst().status());
            verify(appointmentRepository).findByStatusWithDetails(AppointmentStatus.PENDING);
        }

        @Test
        @DisplayName("getAppointmentDocumentPath should return path of document")
        void getAppointmentDocumentPath_ShouldReturnPath() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
            String path = appointmentService.getAppointmentDocumentPath(1L);
            assertEquals("document.pdf", path);
        }

        @Test
        @DisplayName("getAppointmentDocumentPath should throw exception if appointment does not exist")
        void getAppointmentDocumentPath_AppointmentNotFound_ShouldThrowException() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> appointmentService.getAppointmentDocumentPath(1L));
        }

        @Test
        @DisplayName("getAppointmentDocumentPath should throw exception if file path is null")
        void getAppointmentDocumentPath_PathIsNull_ShouldThrowException() {
            testAppointment.setDocumentPath(null);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
            assertThrows(ResourceNotFoundException.class, () -> appointmentService.getAppointmentDocumentPath(1L));
        }
    }

    @Nested
    @DisplayName("actions on appointments")
    class ModifyAppointmentStatusTests {

        @Test
        @DisplayName("cancelAppointment should succeed if PENDING status")
        void cancelAppointment_PendingStatus_ShouldSucceed() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

            appointmentService.cancelAppointment(1L);

            assertEquals(AppointmentStatus.CANCELLED, testAppointment.getStatus());
            assertTrue(testTimeSlot.isAvailable());
            verify(appointmentRepository).save(testAppointment);
            verify(notificationService).createAndSendNotification(any(), anyString());
        }

        @Test
        @DisplayName("cancelAppointment should succeed if APPROVED status")
        void cancelAppointment_ApprovedStatus_ShouldSucceed() {
            testAppointment.setStatus(AppointmentStatus.APPROVED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

            appointmentService.cancelAppointment(1L);

            assertEquals(AppointmentStatus.CANCELLED, testAppointment.getStatus());
            assertTrue(testTimeSlot.isAvailable());
        }

        @Test
        @DisplayName("cancelAppointment should throw IllegalAppointmentOperationException for COMPLETED status")
        void cancelAppointment_WrongStatus_ShouldThrowException() {
            testAppointment.setStatus(AppointmentStatus.COMPLETED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

            assertThrows(IllegalAppointmentOperationException.class, () -> appointmentService.cancelAppointment(1L));
        }

        @Test
        @DisplayName("approveAppointment should succeed for PENDING status")
        void approveAppointment_PendingStatus_ShouldSucceed() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

            appointmentService.approveAppointment(1L);

            assertEquals(AppointmentStatus.APPROVED, testAppointment.getStatus());
            verify(appointmentRepository).save(testAppointment);
            verify(notificationService).createAndSendNotification(any(), anyString());
        }

        @Test
        @DisplayName("approveAppointment should throw IllegalAppointmentOperationException for APPROVED status")
        void approveAppointment_WrongStatus_ShouldThrowException() {
            testAppointment.setStatus(AppointmentStatus.APPROVED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

            assertThrows(IllegalAppointmentOperationException.class, () -> appointmentService.approveAppointment(1L));
        }

        @Test
        @DisplayName("rejectAppointment should work for PENDING status")
        void rejectAppointment_PendingStatus_ShouldSucceed() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
            String reason = "Invalid document";

            appointmentService.rejectAppointment(1L, reason);

            assertEquals(AppointmentStatus.REJECTED, testAppointment.getStatus());
            assertEquals(reason, testAppointment.getRejectionReason());
            assertTrue(testTimeSlot.isAvailable());
            verify(appointmentRepository).save(testAppointment);
            verify(notificationService).createAndSendNotification(any(), anyString());
        }

        @Test
        @DisplayName("rejectAppointment should throw IllegalAppointmentOperationException for REJECTED status")
        void rejectAppointment_WrongStatus_ShouldThrowException() {
            testAppointment.setStatus(AppointmentStatus.REJECTED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

            assertThrows(IllegalAppointmentOperationException.class, () -> appointmentService.rejectAppointment(1L, "reason"));
        }

        @Test
        @DisplayName("completeAppointment should succeed for APPROVED status")
        void completeAppointment_ApprovedStatus_ShouldSucceed() {
            testAppointment.setStatus(AppointmentStatus.APPROVED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

            appointmentService.completeAppointment(1L);

            assertEquals(AppointmentStatus.COMPLETED, testAppointment.getStatus());
            verify(appointmentRepository).save(testAppointment);
            verify(notificationService).createAndSendNotification(any(), anyString());
        }

        @Test
        @DisplayName("completeAppointment should throw IllegalAppointmentOperationException for PENDING status")
        void completeAppointment_WrongStatus_ShouldThrowException() {
            testAppointment.setStatus(AppointmentStatus.PENDING);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
            assertThrows(IllegalAppointmentOperationException.class, () -> appointmentService.completeAppointment(1L));
        }
    }

    @Nested
    @DisplayName("Report generation tests")
    class ReportGenerationTests {

        @Test
        @DisplayName("getAppointmentsForReport should use default dates if input was null")
        void getAppointmentsForReport_NullDates_ShouldUseDefaults() {
            appointmentService.getAppointmentsForReport(null, null);
            verify(appointmentRepository).findAllWithTimeSlotBetween(
                    eq(LocalDate.of(2025, 9, 1).atStartOfDay()),
                    any(LocalDateTime.class)
            );
        }

        @Test
        @DisplayName("getAppointmentsForReport should throw exception if startDate is after endDate")
        void getAppointmentsForReport_StartDateAfterEndDate_ShouldThrowException() {
            LocalDate startDate = LocalDate.of(2025, 10, 1);
            LocalDate endDate = LocalDate.of(2025, 9, 1);
            assertThrows(IllegalArgumentException.class, () -> appointmentService.getAppointmentsForReport(startDate, endDate));
        }
    }
}
