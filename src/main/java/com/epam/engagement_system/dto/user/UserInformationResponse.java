package com.epam.engagement_system.dto.user;

import com.epam.engagement_system.domain.ApplicationUser;
import com.epam.engagement_system.domain.enums.Gender;

import java.util.Set;
import java.util.stream.Collectors;

public record UserInformationResponse(
        Long id,
        String firstName,
        String lastName,
        String phoneNumber,
        Gender gender,
        Set<String> roles)
{
    public static UserInformationResponse mapToDto(ApplicationUser user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new UserInformationResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getGender(),
                roles
        );
    }
}
