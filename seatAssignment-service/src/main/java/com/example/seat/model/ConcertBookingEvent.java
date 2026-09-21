package com.example.seat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcertBookingEvent {
    private String correlationId;
    private String concertCode;
    private String customerEmail;
    private Integer ticketQuantity;
}
