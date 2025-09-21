package com.epam.engagement_system.domain;

import com.epam.engagement_system.domain.enums.AppointmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private ApplicationUser applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    @Column(name = "groom_first_name", nullable = false)
    private String groomFirstName;

    @Column(name = "groom_last_name", nullable = false)
    private String groomLastName;

    @Column(name = "bride_first_name", nullable = false)
    private String brideFirstName;

    @Column(name = "bride_last_name", nullable = false)
    private String brideLastName;

    @Column(name = "witness_1_first_name", nullable = false)
    private String witness1FirstName;

    @Column(name = "witness_1_last_name", nullable = false)
    private String witness1LastName;

    @Column(name = "witness_2_first_name", nullable = false)
    private String witness2FirstName;

    @Column(name = "witness_2_last_name", nullable = false)
    private String witness2LastName;

    @Column(name = "witness_3_first_name")
    private String witness3FirstName;

    @Column(name = "witness_3_last_name")
    private String witness3LastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Column(name = "notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "reminder_sent", nullable = false)
    private boolean reminderSent = false;

    @Column(name = "document_path", nullable = false)
    private String documentPath;

    @Column(name = "rejection_reason")
    private String rejectionReason;
}
