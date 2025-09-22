package com.epam.engagement_system.exception.appointment;

public class ExistingPendingAppointmentException extends RuntimeException {
    public ExistingPendingAppointmentException(String message) {
        super(message);
    }
}
