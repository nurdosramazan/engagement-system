package com.epam.engagement_system.controller;

import com.epam.engagement_system.dto.ApiResponse;
import com.epam.engagement_system.dto.user.UpdateProfileRequest;
import com.epam.engagement_system.dto.user.UserInformationResponse;
import com.epam.engagement_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<UserInformationResponse>> getCurrentUser(Object ignored) {
        //get user id from the request
        UserInformationResponse userProfile = userService.getUserDtoByUserId(1L);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "User profile fetched successfully", userProfile));
    }

    @PutMapping("/me/update-info")
    public ResponseEntity<ApiResponse<UserInformationResponse>> updateCurrentUserProfile(Object ignored,
                                                                                     @Valid @RequestBody UpdateProfileRequest request) {
        //get user id from the request
        UserInformationResponse userProfile = userService.updateUserProfile(1L, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Profile updated successfully", userProfile));
    }
}
