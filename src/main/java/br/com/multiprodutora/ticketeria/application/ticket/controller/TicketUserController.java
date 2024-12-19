package br.com.multiprodutora.ticketeria.application.ticket.controller;

import br.com.multiprodutora.ticketeria.application.payment.dto.TicketPDFDTO;
import br.com.multiprodutora.ticketeria.application.payment.service.PaymentService;
import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
public class TicketUserController {

    @Autowired
    private PaymentService paymentService;

    Logger logger = Logger.getLogger(TicketUserController.class.getName());

    @GetMapping("/user-tickets/{userId}")
    @Transactional
    public ResponseEntity<List<TicketPDFDTO>> getTicketsByUserId(@PathVariable String userId) {
        logger.info("Buscando tickets do usuário: " + userId);

        try {
            Long userIdLong = Long.parseLong(userId);
            List<TicketPDFDTO> tickets = paymentService.getTicketWebData(userIdLong);

            if (tickets.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(tickets);
        } catch (NumberFormatException e) {
            logger.info("ID do usuário inválido: " + userId);
            return ResponseEntity.badRequest().body(Collections.emptyList());
        } catch (Exception e) {
            logger.info("Erro ao buscar tickets para o usuário: " + userId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
}
