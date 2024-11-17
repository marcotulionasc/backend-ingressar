package br.com.multiprodutora.ticketeria.domain.model.validation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ticket_validation_log")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class TicketValidationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ticketId;
    private String validationId;
    private LocalDateTime validationTime;
    private String status;

    public TicketValidationLog(Long ticketId, String status) {
        this.ticketId = ticketId;
        this.validationId = UUID.randomUUID().toString();
        this.validationTime = LocalDateTime.now();
        this.status = status;
    }

}

