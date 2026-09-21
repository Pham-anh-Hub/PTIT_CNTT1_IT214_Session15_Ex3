package com.example.notification.service;

import com.example.notification.model.SeatReservedEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    @CircuitBreaker(name = "notificationService", fallbackMethod = "handleNotificationFallback")
    public void sendNotification(SeatReservedEvent event) {
        String correlationId = event.getCorrelationId();
        String customerEmail = event.getCustomerEmail();

        log.info("[NotifyService] Received confirmation for correlationId: {} - Sending email to {}", correlationId, customerEmail);
    }

    public void handleNotificationFallback(SeatReservedEvent event, Throwable throwable) {
        log.error("[NotifyService] Circuit Breaker Fallback triggered for correlationId: {}. Reason: {}", 
                event.getCorrelationId(), throwable.getMessage());
    }
}
