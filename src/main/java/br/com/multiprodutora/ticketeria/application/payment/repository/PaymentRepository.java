package br.com.multiprodutora.ticketeria.application.payment.repository;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByUserId(String userId); //:TODO verificar sé a mesma consulta

    @Query(value = "SELECT * FROM payment p WHERE p.status = :status AND TRIM(p.user_id) = :userId", nativeQuery = true)
    List<Payment> findPaymentsByStatusAndUserId(@Param("status") int status, @Param("userId") String userId);

    List<Payment> findByStatus(Status status);

    List<Payment> findByStatusAndIsTicketsSent(Status status, boolean isTicketsSent);

    Page<Payment> findByTenant(Long tenantId, Pageable pageable);

}


