package com.example.orchestrator.service;

import com.example.orchestrator.model.BookingTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ReservationOrchestrationService {

    public boolean reserveSeats(BookingTransaction transaction) {
        log.info("[Orchestrator-Reservation] Reserving seats for bookingId: {}, concertCode: {}, quantity: {}", 
                transaction.getBookingId(), transaction.getConcertCode(), transaction.getTicketQuantity());
        return true;
    }
}
