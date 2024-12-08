package br.com.multiprodutora.ticketeria.repository;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByUserId(String userId);

    List<Payment> findByStatus(Status status);

    List<Payment> findByStatusAndIsTicketsSent(Status status, boolean isTicketsSent);


}

