package br.com.multiprodutora.ticketeria.domain.model.ticket;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.lot.Lot;
import br.com.multiprodutora.ticketeria.domain.model.shoppingCart.ShoppingCart;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.domain.model.ticket.dto.CreateTicketDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "Ticket")
@Table(name = "ticket")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nameTicket;

    private String startDate;

    private String endDate;

    private Status isTicketActive;

    private LocalDateTime createdAt;

    private String areaTicket;

    @ManyToOne
    @JoinColumn(name = "event_id") // This is the FK
    private Event event;

    @ManyToOne
    @JoinColumn(name = "tenant_id") // This is the FK
    private Tenant tenant;

    @ManyToOne
    @JoinColumn(name = "lot_id") // This is the FK
    private Lot lot;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<Lot> lots;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<ShoppingCart> shoppingCarts;


    public Ticket(CreateTicketDTO data) {
        this.nameTicket = data.nameTicket();
        this.startDate = data.startDate();
        this.endDate = data.endDate();
        this.isTicketActive = Status.PENDING; // Business rule
        this.createdAt = LocalDateTime.now();
        this.event = data.eventId();
        this.tenant = data.tenantId();
        this.areaTicket = data.areaTicket();
    }

}
