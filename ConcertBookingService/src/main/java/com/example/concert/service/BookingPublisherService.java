package com.example.concert.service;

import com.example.concert.config.KafkaTopicConfig;
import com.example.concert.model.ConcertBookingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingPublisherService {

    private final KafkaTemplate<String, ConcertBookingEvent> kafkaTemplate;

    public ConcertBookingEvent publishBookingEvent(ConcertBookingEvent event) {
        log.info("[BookingService] Created booking request with correlationId: {}", event.getCorrelationId());
        kafkaTemplate.send(KafkaTopicConfig.CONCERT_EVENTS_TOPIC, event.getCorrelationId(), event);
        log.info("[BookingService] Published event to topic: {}", KafkaTopicConfig.CONCERT_EVENTS_TOPIC);
        return event;
    }
}
