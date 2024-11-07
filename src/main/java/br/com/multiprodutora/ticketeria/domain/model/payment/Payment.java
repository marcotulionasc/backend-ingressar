package br.com.multiprodutora.ticketeria.domain.model.payment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.payment.PaymentTicket;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

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

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentTicket> paymentTickets = new ArrayList<>();
}
