package com.epam.engagement_system.service;

import com.epam.engagement_system.domain.ApplicationUser;
import com.epam.engagement_system.dto.user.UserInformationResponse;
import com.epam.engagement_system.exception.ResourceNotFoundException;
import com.epam.engagement_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserInformationResponse getUserDtoByUserId(Long id) {
        ApplicationUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return UserInformationResponse.mapToDto(user);
    }

}
