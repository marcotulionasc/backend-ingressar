package br.com.multiprodutora.ticketeria.adapters;

import br.com.multiprodutora.ticketeria.domain.model.payment.dto.TicketPDFDTO;
import br.com.multiprodutora.ticketeria.service.PaymentService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Transactional
    @GetMapping("/{paymentId}/ticketdata")
    public ResponseEntity<List<TicketPDFDTO>> getTicketPDFData(@PathVariable Long paymentId) {
        try {
            List<TicketPDFDTO> ticketPDFData = paymentService.getTicketPDFData(String.valueOf(paymentId));
            return ResponseEntity.ok(ticketPDFData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Transactional
    @GetMapping("/user/{userId}/ticketdata")
    public ResponseEntity<List<TicketPDFDTO>> getTicketWebData(@PathVariable Long userId) {
        try {
            List<TicketPDFDTO> ticketWebData = paymentService.getTicketWebData(userId);
            if (ticketWebData.isEmpty()) {
                return ResponseEntity.ok().body(ticketWebData);
            }
            return ResponseEntity.ok(ticketWebData);
        } catch (Exception e) {

            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
