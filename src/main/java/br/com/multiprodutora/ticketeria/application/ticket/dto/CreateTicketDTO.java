package br.com.multiprodutora.ticketeria.application.ticket.dto;

import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;

public record CreateTicketDTO(


        String nameTicket,

        String startDate,

        String endDate,

        String areaTicket,

        Tenant tenantId,

        Event eventId
) {
}
