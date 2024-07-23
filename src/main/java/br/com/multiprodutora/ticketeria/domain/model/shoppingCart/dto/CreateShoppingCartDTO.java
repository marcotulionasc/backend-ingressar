package br.com.multiprodutora.ticketeria.domain.model.shoppingCart.dto;

import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.lot.Lot;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import br.com.multiprodutora.ticketeria.domain.model.user.User;

public record CreateShoppingCartDTO(
        int quantity,
        User userId,
        Event eventId,
        Ticket ticketId,
        Lot lotId
) {
}
