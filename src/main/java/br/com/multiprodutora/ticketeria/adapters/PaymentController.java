package br.com.multiprodutora.ticketeria.adapters;

import br.com.multiprodutora.ticketeria.domain.model.payment.dto.TicketPDFDTO;
import br.com.multiprodutora.ticketeria.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{paymentId}/ticketdata")
    public ResponseEntity<List<TicketPDFDTO>> getTicketPDFData(@PathVariable Long paymentId) {
        try {
            List<TicketPDFDTO> ticketPDFData = paymentService.getTicketPDFData(paymentId);
            return ResponseEntity.ok(ticketPDFData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
