package com.epam.engagement_system.service;

import com.epam.engagement_system.domain.ApplicationUser;
import com.epam.engagement_system.domain.Notification;
import com.epam.engagement_system.dto.NotificationResponse;
import com.epam.engagement_system.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService unit tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private TwilioSmsService twilioSmsService;
    @Spy
    @InjectMocks
    private NotificationService notificationService;

    private ApplicationUser testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUser = new ApplicationUser("+7702123123");
        testUser.setId(1L);

        testNotification = new Notification(testUser, "Test message");
        testNotification.setId(10L);
    }

    @Nested
    @DisplayName("Finding notifications")
    class FindNotificationTests {
        @Test
        @DisplayName("findByUserId should return a list of notifications")
        void findByUserId_ShouldReturnListOfResponses() {
            when(notificationRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId()))
                    .thenReturn(Collections.singletonList(testNotification));

            List<NotificationResponse> responses = notificationService.findByUserId(testUser.getId());

            assertNotNull(responses);
            assertEquals(1, responses.size());
            assertEquals("Test message", responses.getFirst().message());
            assertEquals(10L, responses.getFirst().id());
        }
    }

    @Nested
    @DisplayName("Async and Database Operations")
    class AsyncAndDbTests {
        @Test
        @DisplayName("saveNotification should save a notification using repository")
        void saveNotification_ShouldSaveToRepo() {
            when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

            Notification savedNotification = notificationService.saveNotification(testUser, "A real message");

            assertNotNull(savedNotification);
            assertEquals(10L, savedNotification.getId());
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("sendUserNotificationAsync should send notificatons to websocket and SMS messages")
        void sendUserNotificationAsync_ShouldSendWsAndSms() {
            notificationService.sendUserNotificationAsync(testNotification, testUser.getPhoneNumber());

            verify(messagingTemplate).convertAndSendToUser(
                    eq(testUser.getPhoneNumber()),
                    eq("/queue/notifications"),
                    eq(testNotification)
            );
            verify(twilioSmsService).sendMessageAsync(testUser.getPhoneNumber(), testNotification.getMessage());
        }

        @Test
        @DisplayName("sendAdminTopicNotificationAsync should send websocket message to admin's topic")
        void sendAdminTopicNotificationAsync_ShouldSendToTopic() {
            String message = "Admin topic message";

            notificationService.sendAdminTopicNotificationAsync(message);

            verify(messagingTemplate).convertAndSend("/topic/admin/new-appointments", message);
        }
    }

    @Nested
    @DisplayName("Marking notifications as read")
    class MarkAsReadTests {
        @Test
        @DisplayName("markAllNotificationsAsRead should mark unread notifications as read")
        void markAllNotificationsAsRead_WithUnread_ShouldUpdate() {
            List<Notification> unread = List.of(new Notification(testUser, "test1"), new Notification(testUser, "test2"));
            when(notificationRepository.findByUserIdAndIsReadFalse(testUser.getId())).thenReturn(unread);

            notificationService.markAllNotificationsAsRead(testUser.getId());

            verify(notificationRepository).findByUserIdAndIsReadFalse(testUser.getId());
            unread.forEach(n -> assertTrue(n.isRead()));
        }

        @Test
        @DisplayName("markAllNotificationsAsRead should do nothing if there are no unread notifications")
        void markAllNotificationsAsRead_NoUnread_ShouldDoNothing() {
            when(notificationRepository.findByUserIdAndIsReadFalse(testUser.getId())).thenReturn(Collections.emptyList());

            notificationService.markAllNotificationsAsRead(testUser.getId());

            verify(notificationRepository).findByUserIdAndIsReadFalse(anyLong());
        }
    }
}
