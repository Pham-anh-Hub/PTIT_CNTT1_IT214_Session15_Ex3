package com.example.orchestrator.controller;

import com.example.orchestrator.machine.ConcertBookingStateMachine;
import com.example.orchestrator.model.BookingTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orchestrator/bookings")
@RequiredArgsConstructor
public class OrchestratorController {

    private final ConcertBookingStateMachine stateMachine;

    @PostMapping
    public ResponseEntity<BookingTransaction> createBooking(@RequestBody BookingTransaction transaction) {
        stateMachine.process(transaction);
        return ResponseEntity.ok(transaction);
    }
}
