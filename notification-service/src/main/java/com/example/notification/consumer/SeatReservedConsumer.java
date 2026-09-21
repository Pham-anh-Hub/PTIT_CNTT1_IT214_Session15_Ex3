package com.example.notification.consumer;

import com.example.notification.model.SeatReservedEvent;
import com.example.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatReservedConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "seat-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeSeatReservedEvent(SeatReservedEvent event) {
        notificationService.sendNotification(event);
    }
}
