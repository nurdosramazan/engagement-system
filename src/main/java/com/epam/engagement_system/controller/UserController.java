package com.epam.engagement_system.controller;

import com.epam.engagement_system.dto.ApiResponse;
import com.epam.engagement_system.dto.user.UpdateProfileRequest;
import com.epam.engagement_system.dto.user.UserInformationResponse;
import com.epam.engagement_system.security.CurrentUser;
import com.epam.engagement_system.security.UserPrincipal;
import com.epam.engagement_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserInformationResponse>> getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        UserInformationResponse userProfile = userService.findById(userPrincipal.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "User profile fetched successfully", userProfile));
    }

    @PutMapping("/me/update-info")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserInformationResponse>> updateCurrentUserProfile(@CurrentUser UserPrincipal userPrincipal,
                                                                                     @Valid @RequestBody UpdateProfileRequest request) {
        UserInformationResponse userProfile = userService.updateUserProfile(userPrincipal.getId(), request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Profile updated successfully", userProfile));
    }
}
