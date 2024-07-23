package br.com.multiprodutora.ticketeria.domain.model.ticket.dto;

public record TicketsDTO(
        String nameTicket,

        String isTicketActive,

        String areaTicket,

        String nameLot,

        String priceTicket,

        int amountTicket,

        int taxPriceTicket
) {
}
