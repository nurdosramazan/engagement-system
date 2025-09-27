package com.epam.engagement_system.service;

import com.epam.engagement_system.domain.ApplicationUser;
import com.epam.engagement_system.domain.Notification;
import com.epam.engagement_system.domain.enums.RoleType;
import com.epam.engagement_system.dto.NotificationResponse;
import com.epam.engagement_system.repository.NotificationRepository;
import com.epam.engagement_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationService self;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TwilioSmsService twilioSmsService;

    public NotificationService(@Lazy NotificationService self,
                               NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               SimpMessagingTemplate messagingTemplate,
                               TwilioSmsService twilioSmsService) {
        this.self = self;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.twilioSmsService = twilioSmsService;
    }

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Transactional(readOnly = true)
    public List<NotificationResponse> findByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notification -> new NotificationResponse(
                        notification.getId(), notification.getMessage(), notification.isRead(), notification.getCreatedAt()
                ))
                .toList();
    }

    public void createAndSendNotification(ApplicationUser user, String message) {
        Notification notification = self.saveNotification(user, message);
        NotificationResponse response = new NotificationResponse(
                notification.getId(), notification.getMessage(), notification.isRead(), notification.getCreatedAt()
        );
        self.sendUserNotificationAsync(response, user.getPhoneNumber());
        logger.info("Notification for user {} was created. Requested sending.", user.getId());
    }

    public void createAndSendAdminNotifications(String message) {
        List<ApplicationUser> admins = userRepository.findByRoles_Name(RoleType.ADMIN);

        admins.forEach(admin -> {
            self.saveNotification(admin, message);
            logger.info("Notification for admin {} was created. Requested sending.", admin.getId());
        });

        self.sendAdminTopicNotificationAsync(message);
    }

    @Transactional
    public Notification saveNotification(ApplicationUser user, String message) {
        logger.info("Saving notification for user {}: '{}' in the database", user.getId(), message);
        Notification notification = new Notification(user, message);
        return notificationRepository.save(notification);
    }

    @Async("asyncExecutor")
    public void sendUserNotificationAsync(NotificationResponse notification, String phoneNumber) {
        logger.info("Asynchronously sending notification {} to websocket and SMS", notification.id());

        messagingTemplate.convertAndSendToUser(phoneNumber, "/queue/notifications", notification);
        twilioSmsService.sendMessageAsync(phoneNumber, notification.message());
    }


    @Async("asyncExecutor")
    public void sendAdminTopicNotificationAsync(String message) {
        logger.info("Creating admin notification message: {}", message);
        messagingTemplate.convertAndSend("/topic/admin/new-appointments", message);
    }

    @Transactional
    public void markAllNotificationsAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        if (!unreadNotifications.isEmpty()) {
            unreadNotifications.forEach(notification -> notification.setRead(true));
            logger.info("Marked {} notifications as read for user {}", unreadNotifications.size(), userId);
        }
    }
}
