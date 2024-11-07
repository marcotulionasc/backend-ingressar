package br.com.multiprodutora.ticketeria.domain.model.lot;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.lot.dto.CreateLotDTO;
import br.com.multiprodutora.ticketeria.domain.model.shoppingCart.ShoppingCart;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity(name = "Lot")
@Table(name = "lot")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Lot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nameLot;
    private String priceTicket;
    private int amountTicket;
    private int taxPriceTicket;
    private int orderLot;
    private Status isLotActive;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @OneToMany(mappedBy = "lot", cascade = CascadeType.ALL)
    private List<ShoppingCart> shoppingCarts;

    public Lot(CreateLotDTO data) {
        this.nameLot = data.nameLot();
        this.priceTicket = data.priceTicket();
        this.amountTicket = data.amountTicket();
        this.taxPriceTicket = data.taxPriceTicket();
        this.orderLot = data.orderLot();
        this.isLotActive = Status.ACTIVE; // Regra de negócio
    }
}


