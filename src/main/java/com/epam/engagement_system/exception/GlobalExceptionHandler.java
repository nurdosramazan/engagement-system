package com.epam.engagement_system.exception;

import com.epam.engagement_system.dto.ApiResponse;
import com.epam.engagement_system.exception.appointment.IllegalAppointmentOperationException;
import com.epam.engagement_system.exception.appointment.ExistingPendingAppointmentException;
import com.epam.engagement_system.exception.appointment.InvalidWitnessException;
import com.epam.engagement_system.exception.appointment.TimeSlotNotAvailableException;
import com.epam.engagement_system.exception.auth.OTPNotFoundException;
import com.epam.engagement_system.exception.user.ProfileIncompleteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, exception.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<FieldError>>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, "Invalid data provided.", fieldErrors));
    }

    @ExceptionHandler(ExistingPendingAppointmentException.class)
    public ResponseEntity<ApiResponse<Object>> handleExistingPendingAppointment(ExistingPendingAppointmentException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(false, exception.getMessage(), null));
    }

    @ExceptionHandler(ProfileIncompleteException.class)
    public ResponseEntity<ApiResponse<Object>> handleProfileIncomplete(ProfileIncompleteException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, exception.getMessage(), null));
    }

    @ExceptionHandler(TimeSlotNotAvailableException.class)
    public ResponseEntity<ApiResponse<Object>> handleTimeSlotNotAvailable(TimeSlotNotAvailableException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(false, exception.getMessage(), null));
    }

    @ExceptionHandler(InvalidWitnessException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidWitness(InvalidWitnessException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, exception.getMessage(), null));
    }

    @ExceptionHandler(IllegalAppointmentOperationException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalAppointmentOperation(IllegalAppointmentOperationException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, exception.getMessage(), null));
    }

    @ExceptionHandler(OTPNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleOTPNotFound(OTPNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, exception.getMessage(), null));
    }
}
