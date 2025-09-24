package com.epam.engagement_system.service;

import com.epam.engagement_system.domain.ApplicationUser;
import com.epam.engagement_system.domain.Role;
import com.epam.engagement_system.domain.enums.RoleType;
import com.epam.engagement_system.dto.user.UpdateProfileRequest;
import com.epam.engagement_system.dto.user.UserInformationResponse;
import com.epam.engagement_system.exception.ResourceNotFoundException;
import com.epam.engagement_system.repository.RoleRepository;
import com.epam.engagement_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public UserInformationResponse getUserDtoByUserId(Long userId) {
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

    public void findUserByPhoneNumber(String phoneNumber) {
        userRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    Role userRole = roleRepository.findByName(RoleType.USER)
                            .orElseThrow(() -> new RuntimeException("Error: Default USER role not found."));

                    ApplicationUser user = new ApplicationUser(phoneNumber);
                    user.setRoles(Set.of(userRole));
                    return userRepository.save(user);
                });
    }

    private String normalizeName(String name) {
        name = name.trim().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
