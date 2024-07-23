package br.com.multiprodutora.ticketeria.domain.model.ticket.dto;

import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.domain.model.ticket.AreaTicket;

public record CreateTicketDTO(


        String nameTicket,

        String startDate,

        String endDate,

        AreaTicket areaTicket,

        Tenant tenantId,

        Event eventId
) {
}
