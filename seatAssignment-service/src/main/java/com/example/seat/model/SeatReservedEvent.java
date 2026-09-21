package com.example.seat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatReservedEvent {
    private String correlationId;
    private String concertCode;
    private String customerEmail;
    private Integer ticketQuantity;
    private List<String> assignedSeats;
    private String status;
    private String message;
}
