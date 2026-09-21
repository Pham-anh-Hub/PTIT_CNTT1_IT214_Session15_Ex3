package com.example.seat.service;

import com.example.seat.model.ReservationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SeatReservationService {

    public boolean reserveSeats(ReservationRequest request) {
        log.info("[ConcertService] Reserving {} seats for concertCode: {} (bookingId: {})", 
                request.getTicketQuantity(), request.getConcertCode(), request.getBookingId());

        List<String> assignedSeats = new ArrayList<>();
        int quantity = request.getTicketQuantity() != null ? request.getTicketQuantity() : 1;
        for (int i = 1; i <= quantity; i++) {
            assignedSeats.add(request.getConcertCode() + "-S" + i);
        }

        log.info("[ConcertService] Assigned seats: {}", assignedSeats);
        return true;
    }
}
