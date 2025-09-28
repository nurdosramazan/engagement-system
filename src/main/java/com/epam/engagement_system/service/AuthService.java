package com.epam.engagement_system.service;

import com.epam.engagement_system.dto.auth.OTPVerificationRequest;
import com.epam.engagement_system.dto.auth.OTPVerificationResponse;
import com.epam.engagement_system.exception.auth.OTPNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TwilioSmsService twilioSmsService;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final Map<String, OTPEntity> otpStore = new ConcurrentHashMap<>();

    public void requestOTP(String phoneNumber) {
        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        otpStore.put(phoneNumber, new OTPEntity(otp, Instant.now().plusSeconds(300)));

        String message = "Your code for login: " + otp;
        twilioSmsService.sendMessageAsync(phoneNumber, message);
        logger.info("Request for OTP code was sent to Twilio API.");
    }

    @Transactional
    public OTPVerificationResponse verifyOtpAndLogin(OTPVerificationRequest request) {
        String phoneNumber = request.phoneNumber();
        String submittedOtp = request.otp();

        OTPEntity otpEntity = otpStore.get(phoneNumber);
        if (otpEntity == null || Instant.now().isAfter(otpEntity.expiresAt()) || !otpEntity.code().equals(submittedOtp)) {
            throw new OTPNotFoundException("The code is not valid or has expired. Please try again.");
        }
        otpStore.remove(phoneNumber);

        UserDetails userDetails = userDetailsService.loadUserByUsername(phoneNumber);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        logger.info("User {} successfully verified OTP. Generating JWT token.", phoneNumber);
        String jwtToken = jwtService.generateJwtToken(authentication);
        return new OTPVerificationResponse(jwtToken, "Bearer");
    }

    record OTPEntity(String code, Instant expiresAt) {}
}
