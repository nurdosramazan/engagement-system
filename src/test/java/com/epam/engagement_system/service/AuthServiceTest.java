package com.epam.engagement_system.service;

import com.epam.engagement_system.dto.auth.OTPVerificationRequest;
import com.epam.engagement_system.dto.auth.OTPVerificationResponse;
import com.epam.engagement_system.exception.auth.OTPNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService unit tests")
class AuthServiceTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private TwilioSmsService twilioSmsService;

    @InjectMocks
    private AuthService authService;

    private Map<String, AuthService.OTPEntity> otpStore;

    private final String testPhoneNumber = "+7702123123";
    private final String testOtp = "555555";
    private UserDetails testUserDetails;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        testUserDetails = new User(testPhoneNumber, "", Collections.emptyList());

        Field otpStoreField = AuthService.class.getDeclaredField("otpStore");
        otpStoreField.setAccessible(true);
        otpStore = (Map<String, AuthService.OTPEntity>) otpStoreField.get(authService);
        otpStore.clear();
    }

    @Nested
    @DisplayName("Requesting OTP")
    class RequestOTPTests {
        @Test
        @DisplayName("requestOTP should generate OTP, store it, and send an SMS")
        void requestOTP_ShouldStoreAndSendSms() {
            authService.requestOTP(testPhoneNumber);

            verify(twilioSmsService).sendMessageAsync(eq(testPhoneNumber), anyString());

            assertFalse(otpStore.isEmpty());
            assertEquals(1, otpStore.size());
            assertTrue(otpStore.containsKey(testPhoneNumber));
        }
    }

    @Nested
    @DisplayName("Verifying OTP and login")
    class VerifyOTPTests {

        private OTPVerificationRequest verificationRequest;

        @BeforeEach
        void setupVerify() {
            verificationRequest = new OTPVerificationRequest(testPhoneNumber, testOtp);
            AuthService.OTPEntity otpEntity = new AuthService.OTPEntity(testOtp, Instant.now().plusSeconds(300));
            otpStore.put(testPhoneNumber, otpEntity);
        }

        @Test
        @DisplayName("verifyOtpAndLogin should return jwt with OTP")
        void verifyOtpAndLogin_ValidOtp_ShouldSucceed() {
            when(userDetailsService.loadUserByUsername(testPhoneNumber)).thenReturn(testUserDetails);
            when(jwtService.generateJwtToken(any(Authentication.class))).thenReturn("test.jwt.token");

            OTPVerificationResponse response = authService.verifyOtpAndLogin(verificationRequest);

            assertNotNull(response);
            assertEquals("test.jwt.token", response.accessToken());
            assertEquals("Bearer", response.tokenType());
            assertTrue(otpStore.isEmpty());
            verify(jwtService).generateJwtToken(any(Authentication.class));
        }

        @Test
        @DisplayName("verifyOtpAndLogin should throw OTPNotFoundException if invalid OTP")
        void verifyOtpAndLogin_InvalidOtp_ShouldThrowException() {
            OTPVerificationRequest badRequest = new OTPVerificationRequest(testPhoneNumber, "654321");

            assertThrows(OTPNotFoundException.class, () -> authService.verifyOtpAndLogin(badRequest));
            assertFalse(otpStore.isEmpty());
        }

        @Test
        @DisplayName("verifyOtpAndLogin should throw OTPNotFoundException if a phone number does not exisst")
        void verifyOtpAndLogin_NonExistentNumber_ShouldThrowException() {
            OTPVerificationRequest badRequest = new OTPVerificationRequest("+0987654321", testOtp);

            assertThrows(OTPNotFoundException.class, () -> authService.verifyOtpAndLogin(badRequest));
        }

        @Test
        @DisplayName("verifyOtpAndLogin should throw OTPNotFoundException for expired OTP")
        void verifyOtpAndLogin_ExpiredOtp_ShouldThrowException() {
            AuthService.OTPEntity expiredOtpEntity = new AuthService.OTPEntity(testOtp, Instant.now().minusSeconds(1));
            otpStore.put(testPhoneNumber, expiredOtpEntity);

            assertThrows(OTPNotFoundException.class, () -> authService.verifyOtpAndLogin(verificationRequest));
        }
    }
}

