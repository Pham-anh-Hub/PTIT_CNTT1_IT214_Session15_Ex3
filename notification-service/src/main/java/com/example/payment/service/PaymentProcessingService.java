package com.example.payment.service;

import com.example.payment.model.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentProcessingService {

    public boolean processPayment(PaymentRequest request) {
        log.info("[PaymentService] Processing payment of amount {} for bookingId: {}", 
                request.getAmount(), request.getBookingId());
        // Simulating successful payment processing
        return true;
    }

    public void refund(PaymentRequest request) {
        log.info("[PaymentService] Refunding payment for bookingId: {}", request.getBookingId());
    }
}
