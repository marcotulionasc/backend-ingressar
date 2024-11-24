package br.com.multiprodutora.ticketeria.domain.model.payment;

import java.time.LocalDateTime;


import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import jakarta.persistence.*;
import lombok.*;


@Entity(name = "Payment")
@Table(name = "payment")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Payment {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    private String userId;
    private String userName;
    private String userEmail;
    private Status paymentStatus;
    private LocalDateTime createdAt;
    private Boolean isTicketActive;
    private Double totalAmount;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn
    private Tenant tenant;

    @Lob
    @Column(name = "selected_tickets_json")
    private String selectedTicketsJson;

}
