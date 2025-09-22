package com.epam.engagement_system.dto.appointment;

public class IllegalAppointmentOperationException extends RuntimeException {
    public IllegalAppointmentOperationException(String message) {
        super(message);
    }
}
