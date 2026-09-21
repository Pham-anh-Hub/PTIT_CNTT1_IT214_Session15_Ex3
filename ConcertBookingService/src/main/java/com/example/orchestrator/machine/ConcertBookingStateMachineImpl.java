package com.example.orchestrator.machine;

import com.example.orchestrator.listener.StateChangeListener;
import com.example.orchestrator.model.BookingEvent;
import com.example.orchestrator.model.BookingState;
import com.example.orchestrator.model.BookingTransaction;
import com.example.orchestrator.model.RetryPolicy;
import com.example.orchestrator.service.PaymentOrchestrationService;
import com.example.orchestrator.service.ReservationOrchestrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ConcertBookingStateMachineImpl implements ConcertBookingStateMachine {

    private final PaymentOrchestrationService paymentService;
    private final ReservationOrchestrationService reservationService;
    private final StateChangeListener stateChangeListener;
    private final RetryPolicy retryPolicy;

    // Save state of each transaction
    private final Map<String, BookingState> transactionStates = new ConcurrentHashMap<>();

    public ConcertBookingStateMachineImpl(PaymentOrchestrationService paymentService,
                                          ReservationOrchestrationService reservationService,
                                          StateChangeListener stateChangeListener) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
        this.stateChangeListener = stateChangeListener;
        this.retryPolicy = new RetryPolicy(3, 2000);
    }

    @Override
    public void process(BookingTransaction transaction) {
        String bookingId = transaction.getBookingId();
        BookingState currentState = transactionStates.getOrDefault(bookingId, transaction.getCurrentState());
        transaction.setCurrentState(currentState);

        switch (currentState) {
            case INITIATED:
                transition(transaction, BookingState.PAYMENT_PENDING, BookingEvent.PROCESS_PAYMENT);
                processPayment(transaction);
                break;

            case PAYMENT_PENDING:
                // Waiting for payment result (handled inside processPayment)
                break;

            case PAYMENT_COMPLETED:
                transition(transaction, BookingState.SEAT_RESERVING, BookingEvent.RESERVE_SEATS);
                processReservation(transaction);
                break;

            case SEAT_RESERVING:
                // Waiting for reservation result (handled inside processReservation)
                break;

            case BOOKING_CONFIRMED:
                log.info("[Orchestrator] Final State: BOOKING_CONFIRMED for booking {}", bookingId);
                break;

            case CANCELLED:
                log.info("[Orchestrator] Transaction {} is CANCELLED", bookingId);
                break;

            default:
                log.error("Unknown state: {}", currentState);
        }
    }

    private void transition(BookingTransaction transaction, BookingState newState, BookingEvent event) {
        String bookingId = transaction.getBookingId();
        BookingState oldState = transactionStates.getOrDefault(bookingId, BookingState.INITIATED);
        transactionStates.put(bookingId, newState);
        transaction.setCurrentState(newState);

        stateChangeListener.onStateChanged(bookingId, oldState, newState, event);
        log.info("[Orchestrator] State: {} -> Event: {} -> New State: {}", oldState, event, newState);
    }

    private void processPayment(BookingTransaction transaction) {
        String bookingId = transaction.getBookingId();

        for (int attempt = 1; attempt <= retryPolicy.getMaxAttempts(); attempt++) {
            try {
                log.info("[Orchestrator] RetryPolicy: Activity 'processPayment' - Attempt {}/{}",
                        attempt, retryPolicy.getMaxAttempts());
                boolean success = paymentService.processPayment(transaction);
                if (success) {
                    transition(transaction, BookingState.PAYMENT_COMPLETED, BookingEvent.PAYMENT_SUCCESS);
                    process(transaction);
                    return;
                }
            } catch (Exception e) {
                if (attempt == retryPolicy.getMaxAttempts()) {
                    transition(transaction, BookingState.CANCELLED, BookingEvent.PAYMENT_FAILED);
                    compensationTransaction(transaction);
                    return;
                }
                try {
                    Thread.sleep(retryPolicy.getDelayMs());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void processReservation(BookingTransaction transaction) {
        try {
            boolean success = reservationService.reserveSeats(transaction);
            if (success) {
                transition(transaction, BookingState.BOOKING_CONFIRMED, BookingEvent.RESERVATION_SUCCESS);
                process(transaction);
            } else {
                transition(transaction, BookingState.CANCELLED, BookingEvent.RESERVATION_FAILED);
                compensationTransaction(transaction);
            }
        } catch (Exception e) {
            transition(transaction, BookingState.CANCELLED, BookingEvent.RESERVATION_FAILED);
            compensationTransaction(transaction);
        }
    }

    private void compensationTransaction(BookingTransaction transaction) {
        log.info("[Orchestrator] Compensation triggered for booking: {}", transaction.getBookingId());
        if (transaction.getCurrentState() == BookingState.PAYMENT_COMPLETED) {
            paymentService.refund(transaction);
        }
    }
}
