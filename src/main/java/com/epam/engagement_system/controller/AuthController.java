package com.epam.engagement_system.controller;

import com.epam.engagement_system.dto.ApiResponse;
import com.epam.engagement_system.dto.auth.OTPRequest;
import com.epam.engagement_system.dto.auth.OTPVerificationRequest;
import com.epam.engagement_system.dto.auth.OTPVerificationResponse;
import com.epam.engagement_system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/request-otp")
    public ResponseEntity<ApiResponse<String>> requestOTP(@Valid @RequestBody OTPRequest request) {
        authService.requestOTP(request.phoneNumber());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "OTP has been sent", request.phoneNumber()));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<OTPVerificationResponse>> verifyOTP(@Valid @RequestBody OTPVerificationRequest request) {
        String jwtToken = authService.verifyOtpAndLogin(request);
        OTPVerificationResponse authResponse = new OTPVerificationResponse(jwtToken, "Bearer");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Login successful", authResponse));
    }
}
