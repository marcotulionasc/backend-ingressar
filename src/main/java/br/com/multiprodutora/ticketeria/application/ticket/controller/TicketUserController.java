package br.com.multiprodutora.ticketeria.application.ticket.controller;

import br.com.multiprodutora.ticketeria.application.payment.service.PaymentService;
import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
public class TicketUserController {

    @Autowired
    private PaymentService paymentService;

    Logger logger = Logger.getLogger(TicketUserController.class.getName());

    @GetMapping("/user-tickets/{userId}")
    public ResponseEntity<List<Payment>> getPaymentsByUserId(@RequestParam String userId){

        logger.info("Buscando pagamentos do usuário: " + userId);

        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        if(payments.isEmpty()){
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(payments);
    }
}
