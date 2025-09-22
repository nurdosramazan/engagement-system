package com.epam.engagement_system.repository;

import com.epam.engagement_system.domain.Appointment;
import com.epam.engagement_system.domain.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.timeSlot " +
            "LEFT JOIN FETCH a.applicant " +
            "WHERE a.applicant.id = :applicantId ORDER BY a.createdAt DESC")
    List<Appointment> findByApplicantId(Long applicantId);

    boolean existsByApplicantIdAndStatusIn(Long applicantId, List<AppointmentStatus> statuses);
}
