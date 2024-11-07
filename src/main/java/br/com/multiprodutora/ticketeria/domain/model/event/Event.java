package br.com.multiprodutora.ticketeria.domain.model.event;


import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.address.Address;
import br.com.multiprodutora.ticketeria.domain.model.config.ConfigEvent;
import br.com.multiprodutora.ticketeria.domain.model.event.dto.CreateEventDTO;
import br.com.multiprodutora.ticketeria.domain.model.lot.Lot;
import br.com.multiprodutora.ticketeria.domain.model.shoppingCart.ShoppingCart;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Entity(name = "Event")
@Table(name = "event")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")

public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nameEvent;

    private String titleEvent;

    private String description;

    private String date;

    private String local;

    private String hourOfStart;

    private String hourOfShow;

    private LocalDateTime createdAt;

    private String imageEvent;

    private String imageFlyer; // será o banner que fica na home

    private Status isEventActive;

    @Embedded
    private Address address;

    @ManyToOne
//    @JoinColumn(name = "tenant_id") // This is the FK
    private Tenant tenant;

    @ManyToOne
    @JoinColumn(name = "config_event_id")
    private ConfigEvent configEvent;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Ticket> tickets;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Lot> lots;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<ShoppingCart> shoppingCarts;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<ConfigEvent> configEvents;


    public Event(CreateEventDTO data) {
        this.nameEvent = data.nameEvent();
        this.titleEvent = data.titleEvent();
        this.description = data.description();
        this.date = data.date();
        this.createdAt = LocalDateTime.now();
        this.isEventActive = Status.ACTIVE; // Business rule
        this.imageEvent = data.imageEvent();
        this.local = data.local();
        this.hourOfStart = data.hourOfStart();
        this.hourOfShow = data.hourOfShow();
        this.tenant = data.tenantId();
        this.imageFlyer = data.imageEvent();
        this.configEvent = data.configEventId();
        this.address = new Address(data.address());
    }
}
