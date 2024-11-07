package br.com.multiprodutora.ticketeria.domain.model.payment;

import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    @JsonBackReference
    private Payment payment;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;
}
