package com.example.payment.controller;

import com.example.payment.model.PaymentRequest;
import com.example.payment.service.PaymentProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentProcessingService paymentProcessingService;

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody PaymentRequest request) {
        boolean success = paymentProcessingService.processPayment(request);
        return ResponseEntity.ok(Map.of("success", success, "bookingId", request.getBookingId()));
    }

    @PostMapping("/refund")
    public ResponseEntity<Map<String, Object>> refundPayment(@RequestBody PaymentRequest request) {
        paymentProcessingService.refund(request);
        return ResponseEntity.ok(Map.of("status", "REFUNDED", "bookingId", request.getBookingId()));
    }
}
