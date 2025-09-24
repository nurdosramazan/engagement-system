package com.epam.engagement_system.exception.appointment;

public class IllegalAppointmentOperationException extends RuntimeException {
    public IllegalAppointmentOperationException(String message) {
        super(message);
    }
}
