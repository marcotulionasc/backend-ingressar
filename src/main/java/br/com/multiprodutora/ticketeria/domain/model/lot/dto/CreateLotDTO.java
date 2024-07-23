package br.com.multiprodutora.ticketeria.domain.model.lot.dto;

import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.lot.Lot;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;


public record CreateLotDTO(
        long id,
        String nameLot,
        String priceTicket,
        int amountTicket,
        int taxPriceTicket,
        int orderLot,
        Long tenantId,
        Long eventId,
        Long ticketId
) {
    public CreateLotDTO(Lot lot) {
        this(
                lot.getId(),
                lot.getNameLot(),
                lot.getPriceTicket(),
                lot.getAmountTicket(),
                lot.getTaxPriceTicket(),
                lot.getOrderLot(),
                lot.getTenant().getId(),
                lot.getEvent().getId(),
                lot.getTicket().getId()
        );
    }
}



