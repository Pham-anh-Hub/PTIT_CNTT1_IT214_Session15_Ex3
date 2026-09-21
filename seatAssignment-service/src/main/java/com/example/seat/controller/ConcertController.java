package com.example.seat.controller;

import com.example.seat.model.ReservationRequest;
import com.example.seat.service.SeatReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final SeatReservationService seatReservationService;

    @PostMapping("/reserve")
    public ResponseEntity<Map<String, Object>> reserveSeats(@RequestBody ReservationRequest request) {
        boolean success = seatReservationService.reserveSeats(request);
        return ResponseEntity.ok(Map.of("success", success, "bookingId", request.getBookingId()));
    }
}
