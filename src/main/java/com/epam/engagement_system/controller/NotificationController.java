package com.epam.engagement_system.controller;

import com.epam.engagement_system.dto.ApiResponse;
import com.epam.engagement_system.dto.NotificationResponse;
import com.epam.engagement_system.security.CurrentUser;
import com.epam.engagement_system.security.UserPrincipal;
import com.epam.engagement_system.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications(@CurrentUser UserPrincipal principal) {
        List<NotificationResponse> notifications = notificationService.findByUserId(principal.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Notifications fetched successfully", notifications));
    }

    @PostMapping("/mark-as-read")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Object>> markAllAsRead(@CurrentUser UserPrincipal principal) {
        notificationService.markAllNotificationsAsRead(principal.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "All notifications marked as read.", null));
    }
}
