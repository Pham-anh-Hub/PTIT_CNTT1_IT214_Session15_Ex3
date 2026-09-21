package com.example.seat.producer;

import com.example.seat.config.KafkaTopicConfig;
import com.example.seat.model.SeatReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatEventProducer {

    private final KafkaTemplate<String, SeatReservedEvent> kafkaTemplate;

    public void publishSeatReservedEvent(SeatReservedEvent event) {
        log.info("[SeatService] Publishing SeatReserved event with correlationId: {} to topic: {}", 
                event.getCorrelationId(), KafkaTopicConfig.SEAT_EVENTS_TOPIC);
        kafkaTemplate.send(KafkaTopicConfig.SEAT_EVENTS_TOPIC, event.getCorrelationId(), event);
    }
}
