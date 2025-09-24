package com.epam.engagement_system.exception.auth;

public class OTPNotFoundException extends RuntimeException {
    public OTPNotFoundException(String message) {
        super(message);
    }
}
