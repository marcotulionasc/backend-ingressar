package br.com.multiprodutora.ticketeria.adapters;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ValidateTicketController {

    @Autowired
    private TicketService ticketService;

    @GetMapping("/updateStatus") // TODO: REVISAR ISSO URGENTEMENTE
    public ResponseEntity<String> updateTicketStatus(
            @RequestParam Long ticketId,
            @RequestParam Status status) {
        boolean isUpdated = ticketService.updateStatus(ticketId, status);
        if (isUpdated) {
            return ResponseEntity.ok().body("Ingresso validado com sucesso!");
        } else {
            return ResponseEntity.internalServerError().body("Erro ao validar o ingresso...");
        }
    }
}
