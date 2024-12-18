package br.com.multiprodutora.ticketeria.application.ticket.dto;

import br.com.multiprodutora.ticketeria.domain.Status;

import java.time.LocalDateTime;

public record TicketDTO(
        long id,
        String nameTicket,
        String startDate,
        String endDate,
        Status isTicketActive,
        LocalDateTime createdAt,
        String areaTicket // Enum
) {}
