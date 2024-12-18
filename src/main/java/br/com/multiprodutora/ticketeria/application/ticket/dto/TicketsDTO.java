package br.com.multiprodutora.ticketeria.application.ticket.dto;

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
