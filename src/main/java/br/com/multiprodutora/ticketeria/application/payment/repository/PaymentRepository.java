package br.com.multiprodutora.ticketeria.application.payment.repository;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByUserId(String userId);

    List<Payment> findByStatus(Status status);

    List<Payment> findByStatusAndIsTicketsSent(Status status, boolean isTicketsSent);

    Page<Payment> findByTenant(Long tenantId, Pageable pageable);

}


