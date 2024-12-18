package br.com.multiprodutora.ticketeria.application.lot.dto;

import br.com.multiprodutora.ticketeria.domain.model.lot.Lot;


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



