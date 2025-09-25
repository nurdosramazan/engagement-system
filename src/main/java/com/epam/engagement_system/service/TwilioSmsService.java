package com.epam.engagement_system.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TwilioSmsService {

    private static final Logger logger = LoggerFactory.getLogger(TwilioSmsService.class);

    @Value("${twilio.sms.number}")
    private String fromPhoneNumber;

    @Async("asyncExecutor")
    public void sendMessage(String recipient, String messageBody) {
        try {
            PhoneNumber to = new PhoneNumber(recipient);
            PhoneNumber from = new PhoneNumber(fromPhoneNumber);
            Message message = Message.creator(to, from, messageBody).create();
            if (message.getStatus() == Message.Status.FAILED || message.getStatus() == Message.Status.CANCELED) {
                logger.error("Twilio failed to send SMS to {}. Status: {}, Error: {}", recipient, message.getStatus(), message.getErrorMessage());
            } else {
                logger.info("Twilio successfully send message to {}", recipient);
            }
        } catch (Exception e) {
            logger.error("Failed to send SMS message to {}: {}", recipient, e.getMessage(), e);
        }
    }
}
