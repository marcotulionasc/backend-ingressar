package br.com.multiprodutora.ticketeria.application.ticket.repository;

import br.com.multiprodutora.ticketeria.domain.model.validation.TicketValidationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketValidationLogRepository extends JpaRepository<TicketValidationLog, Long> {
}
