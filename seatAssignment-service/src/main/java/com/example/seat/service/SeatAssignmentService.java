package com.example.seat.service;

import com.example.seat.model.ConcertBookingEvent;
import com.example.seat.model.SeatReservedEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SeatAssignmentService {

    @CircuitBreaker(name = "seatAssignmentService", fallbackMethod = "handleSeatAssignmentFallback")
    public SeatReservedEvent assignSeats(ConcertBookingEvent bookingEvent) {
        String correlationId = bookingEvent.getCorrelationId();
        log.info("[SeatService] Received event with correlationId: {}", correlationId);

        // Simulate seat reservation in database
        List<String> assignedSeats = new ArrayList<>();
        int quantity = bookingEvent.getTicketQuantity() != null ? bookingEvent.getTicketQuantity() : 1;
        String concertCode = bookingEvent.getConcertCode() != null ? bookingEvent.getConcertCode() : "CONCERT";
        
        for (int i = 1; i <= quantity; i++) {
            assignedSeats.add(concertCode + "-A" + i);
        }

        log.info("[SeatService] Seat reserved successfully for correlationId: {}", correlationId);

        return SeatReservedEvent.builder()
                .correlationId(correlationId) // Preserve exact correlationId
                .concertCode(bookingEvent.getConcertCode())
                .customerEmail(bookingEvent.getCustomerEmail())
                .ticketQuantity(bookingEvent.getTicketQuantity())
                .assignedSeats(assignedSeats)
                .status("SUCCESS")
                .message("Seats reserved successfully")
                .build();
    }

    public SeatReservedEvent handleSeatAssignmentFallback(ConcertBookingEvent bookingEvent, Throwable throwable) {
        String correlationId = bookingEvent.getCorrelationId();
        log.error("[SeatService] Circuit Breaker Fallback triggered for correlationId: {}. Reason: {}", correlationId, throwable.getMessage());

        return SeatReservedEvent.builder()
                .correlationId(correlationId)
                .concertCode(bookingEvent.getConcertCode())
                .customerEmail(bookingEvent.getCustomerEmail())
                .ticketQuantity(bookingEvent.getTicketQuantity())
                .assignedSeats(List.of())
                .status("FAILED")
                .message("Seat assignment fallback: " + throwable.getMessage())
                .build();
    }
}
