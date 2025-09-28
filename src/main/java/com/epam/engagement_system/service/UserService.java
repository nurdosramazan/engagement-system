package com.epam.engagement_system.service;

import com.epam.engagement_system.domain.ApplicationUser;
import com.epam.engagement_system.dto.user.UpdateProfileRequest;
import com.epam.engagement_system.dto.user.UserInformationResponse;
import com.epam.engagement_system.exception.ResourceNotFoundException;
import com.epam.engagement_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserInformationResponse findById(Long userId) {
        ApplicationUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return UserInformationResponse.mapToDto(user);
    }

    @Transactional
    public UserInformationResponse updateUserProfile(Long userId, UpdateProfileRequest request) {
        ApplicationUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setFirstName(normalizeName(request.firstName()));
        user.setLastName(normalizeName(request.lastName()));
        user.setGender(request.gender());

        return UserInformationResponse.mapToDto(user);
    }

    private String normalizeName(String name) {
        if (name == null) return null;
        name = name.trim().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
