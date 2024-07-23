package br.com.multiprodutora.ticketeria.domain.model.shoppingCart;

import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.lot.Lot;
import br.com.multiprodutora.ticketeria.domain.model.shoppingCart.dto.CreateShoppingCartDTO;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import br.com.multiprodutora.ticketeria.domain.model.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "ShoppingCart")
@Table(name = "shopping_carts")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class ShoppingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "lot_id")
    private Lot lot;

    public ShoppingCart(CreateShoppingCartDTO data) {
        this.quantity = data.quantity();
        this.user = data.userId();
        this.event = data.eventId();
        this.ticket = data.ticketId();
        this.lot = data.lotId();
    }
}
