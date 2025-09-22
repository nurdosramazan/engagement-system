package com.epam.engagement_system.exception.appointment;

public class TimeSlotNotAvailableException extends RuntimeException {
    public TimeSlotNotAvailableException(String message) {
        super(message);
    }
}
