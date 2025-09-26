package com.epam.engagement_system.service;

import com.epam.engagement_system.domain.ApplicationUser;
import com.epam.engagement_system.domain.enums.Gender;
import com.epam.engagement_system.dto.user.UpdateProfileRequest;
import com.epam.engagement_system.dto.user.UserInformationResponse;
import com.epam.engagement_system.exception.ResourceNotFoundException;
import com.epam.engagement_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService unit tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private ApplicationUser testUser;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        testUser = new ApplicationUser("+7702123123");
        testUser.setId(userId);
        testUser.setFirstName("Nurdos");
        testUser.setLastName("Ramazan");
        testUser.setGender(Gender.MALE);
    }

    @Nested
    @DisplayName("Finding the user")
    class FindUserTests {
        @Test
        @DisplayName("findById should return user information")
        void findById_UserExists_ShouldReturnUserInfo() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            UserInformationResponse response = userService.findById(userId);

            assertNotNull(response);
            assertEquals(userId, response.id());
            assertEquals("Nurdos", response.firstName());
            assertEquals(Gender.MALE, response.gender());
        }

        @Test
        @DisplayName("findById should throw ResourceNotFoundException if user does not exist")
        void findById_UserNotFound_ShouldThrowException() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> userService.findById(userId));
        }
    }

    @Nested
    @DisplayName("Updating profile")
    class UpdateUserProfileTests {
        @Test
        @DisplayName("updateUserProfile should return user with updated nanes")
        void updateUserProfile_UserExists_ShouldUpdateAndReturnUserInfo() {
            UpdateProfileRequest updateRequest = new UpdateProfileRequest("Nurdoss", "Ramazann", Gender.MALE);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            UserInformationResponse response = userService.updateUserProfile(userId, updateRequest);

            assertNotNull(response);
            assertEquals(userId, response.id());
            assertEquals("Nurdoss", response.firstName());
            assertEquals("Ramazann", response.lastName());
            assertEquals(Gender.MALE, response.gender());

            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("updateUserProfile should throw ResourceNotFoundException when user does not exist")
        void updateUserProfile_UserNotFound_ShouldThrowException() {
            UpdateProfileRequest updateRequest = new UpdateProfileRequest("nurdos", "ramazan", Gender.FEMALE);
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> userService.updateUserProfile(userId, updateRequest));
        }
    }
}
