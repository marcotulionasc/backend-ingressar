package br.com.multiprodutora.ticketeria.domain.model.lot.dto;

import br.com.multiprodutora.ticketeria.domain.Status;

public record LotDTO(
        long id,
        String nameLot,
        String priceTicket,
        int amountTicket,
        int taxPriceTicket,
        int orderLot,
        Status isLotActive
) {}
