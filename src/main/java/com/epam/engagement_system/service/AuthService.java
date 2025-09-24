package com.epam.engagement_system.service;

import com.epam.engagement_system.dto.auth.OTPVerificationRequest;
import com.epam.engagement_system.exception.auth.OTPNotFoundException;
import lombok.RequiredArgsConstructor;
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
    private final UserService userService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private final Map<String, OTPEntity> otpStore = new ConcurrentHashMap<>();

    public void requestOTP(String phoneNumber) {
        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        otpStore.put(phoneNumber,new OTPEntity(otp, Instant.now().plusSeconds(300)));

        String message = "Your code for login: " + otp;
        //someService.sendMessage(phoneNumber, message);
        System.out.println(message);
    }

    @Transactional
    public String verifyOtpAndLogin(OTPVerificationRequest request) {
        String phoneNumber = request.phoneNumber();
        String submittedOtp = request.otp();

        OTPEntity otpEntity = otpStore.get(phoneNumber);
        if (otpEntity == null || Instant.now().isAfter(otpEntity.expiresAt()) || !otpEntity.code().equals(submittedOtp)) {
            throw new OTPNotFoundException("The code is not valid or has expired. Please try again.");
        }
        otpStore.remove(phoneNumber);

        userService.findUserByPhoneNumber(phoneNumber);
        //todo: refactor

        UserDetails userDetails = userDetailsService.loadUserByUsername(phoneNumber);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        return jwtService.generateJwtToken(authentication);
    }

    private record OTPEntity(String code, Instant expiresAt) {}
}
