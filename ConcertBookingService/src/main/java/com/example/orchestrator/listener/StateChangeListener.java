package com.example.orchestrator.listener;

import com.example.orchestrator.model.BookingEvent;
import com.example.orchestrator.model.BookingState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StateChangeListener {

    public void onStateChanged(String bookingId, BookingState oldState, BookingState newState, BookingEvent event) {
        log.debug("[StateChangeListener] Booking {}: {} -> {} on event {}", bookingId, oldState, newState, event);
    }
}
