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

    @GetMapping("/updateStatus")
    public ResponseEntity<MessageResponse> updateTicketStatus(
            @RequestParam Long ticketId,
            @RequestParam Status status) {
        boolean isUpdated = ticketService.updateStatus(ticketId, status);
        if (isUpdated) {
            return ResponseEntity.ok(new MessageResponse("Status do ticket atualizado com sucesso."));
        } else {
            return ResponseEntity.status(500).body(new MessageResponse("Falha ao atualizar o status do ticket."));
        }
    }

    public static class MessageResponse {
        private String message;

        public MessageResponse() {
        }

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
