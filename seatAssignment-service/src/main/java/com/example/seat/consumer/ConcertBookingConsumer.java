package com.example.seat.consumer;

import com.example.seat.config.KafkaTopicConfig;
import com.example.seat.model.ConcertBookingEvent;
import com.example.seat.model.SeatReservedEvent;
import com.example.seat.producer.SeatEventProducer;
import com.example.seat.service.SeatAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConcertBookingConsumer {

    private final SeatAssignmentService seatAssignmentService;
    private final SeatEventProducer seatEventProducer;

    @KafkaListener(topics = KafkaTopicConfig.CONCERT_EVENTS_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeConcertBookingEvent(ConcertBookingEvent event) {
        SeatReservedEvent reservedEvent = seatAssignmentService.assignSeats(event);
        seatEventProducer.publishSeatReservedEvent(reservedEvent);
    }
}
