package br.com.multiprodutora.ticketeria.repository;

import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}

