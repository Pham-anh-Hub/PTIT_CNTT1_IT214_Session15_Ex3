package com.example.concert.controller;

import com.example.concert.model.ConcertBookingEvent;
import com.example.concert.service.BookingPublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class ConcertBookingController {

    private final BookingPublisherService bookingPublisherService;

    @PostMapping
    public ResponseEntity<ConcertBookingEvent> createBooking(@RequestBody ConcertBookingEvent bookingEvent) {
        ConcertBookingEvent publishedEvent = bookingPublisherService.publishBookingEvent(bookingEvent);
        return ResponseEntity.ok(publishedEvent);
    }
}
