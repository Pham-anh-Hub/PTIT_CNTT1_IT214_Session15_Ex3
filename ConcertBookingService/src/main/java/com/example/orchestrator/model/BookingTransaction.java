package com.example.orchestrator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingTransaction {
    private String bookingId;
    private String concertCode;
    private String customerId;
    private String customerEmail;
    private Integer ticketQuantity;
    private Double amount;
    @Builder.Default
    private BookingState currentState = BookingState.INITIATED;
}
