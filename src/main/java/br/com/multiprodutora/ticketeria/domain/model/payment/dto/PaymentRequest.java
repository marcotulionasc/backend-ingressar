package br.com.multiprodutora.ticketeria.domain.model.payment.dto;


import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.lot.Lot;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;

import java.time.LocalDateTime;

public record PaymentRequest(

         String token,
         Double transactionAmount,
         String description,
         Integer installments,
         String paymentMethodId,
         String email,
         Status paymentStatus,
         LocalDateTime createdAt,
         Event event,
         Ticket ticket,
         Lot lot,
         Tenant tenant
) {
}
