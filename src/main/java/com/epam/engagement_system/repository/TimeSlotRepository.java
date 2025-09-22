package com.epam.engagement_system.repository;

import com.epam.engagement_system.domain.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Set;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    @Query("SELECT ts.startTime FROM TimeSlot ts WHERE ts.startTime BETWEEN :start AND :end")
    Set<LocalDateTime> findExistingStartTimes(LocalDateTime start, LocalDateTime end);
}
