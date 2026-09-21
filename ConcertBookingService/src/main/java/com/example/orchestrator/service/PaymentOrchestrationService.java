package com.example.orchestrator.service;

import com.example.orchestrator.model.BookingTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentOrchestrationService {

    public boolean processPayment(BookingTransaction transaction) {
        log.info("[Orchestrator-Payment] Executing payment for bookingId: {}, amount: {}", 
                transaction.getBookingId(), transaction.getAmount());
        return true;
    }

    public void refund(BookingTransaction transaction) {
        log.info("[Orchestrator-Payment] Refunding payment for bookingId: {}", transaction.getBookingId());
    }
}
