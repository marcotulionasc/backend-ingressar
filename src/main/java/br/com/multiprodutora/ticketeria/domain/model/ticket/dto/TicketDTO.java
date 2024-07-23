package br.com.multiprodutora.ticketeria.domain.model.ticket.dto;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.ticket.AreaTicket;

import java.time.LocalDateTime;

public record TicketDTO(
        long id,
        String nameTicket,
        String startDate,
        String endDate,
        Status isTicketActive,
        LocalDateTime createdAt,
        AreaTicket areaTicket // Enum
) {}
