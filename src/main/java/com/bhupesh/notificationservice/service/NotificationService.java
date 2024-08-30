package com.bhupesh.notificationservice.service;

import com.bhupesh.notificationservice.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.email}")
    private String fromEmail;

    @KafkaListener(topics = "order-placed")
    public void listen(OrderPlacedEvent orderPlacedEvent) throws MailException {
        log.info("Received message: {}", orderPlacedEvent);
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(fromEmail);
            messageHelper.setTo(orderPlacedEvent.getEmail());
            messageHelper.setSubject(MessageFormat.format("Your order with OrderNumber {0} has been placed", orderPlacedEvent.getOrderNumber()));
            messageHelper.setText(MessageFormat.format("""
                    Dear {0} {1},
                    Your order with OrderNumber {2} has been placed successfully.
                    Thanks for shopping with us.
                    """,
                    orderPlacedEvent.getFirstName(),
                    orderPlacedEvent.getLastName(),
                    orderPlacedEvent.getOrderNumber()));
        };
        javaMailSender.send(messagePreparator);
        log.info("Email sent successfully to: {}", orderPlacedEvent.getEmail());

    }
}
