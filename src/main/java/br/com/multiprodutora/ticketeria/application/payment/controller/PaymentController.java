package br.com.multiprodutora.ticketeria.application.payment.controller;

import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import br.com.multiprodutora.ticketeria.application.payment.dto.TicketPDFDTO;
import br.com.multiprodutora.ticketeria.application.payment.repository.PaymentRepository;
import br.com.multiprodutora.ticketeria.application.payment.service.PaymentService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Transactional
    @GetMapping("/user/{userId}/ticketdata")
    public ResponseEntity<List<TicketPDFDTO>> getTicketWebData(@PathVariable Long userId) {
        try {
            List<TicketPDFDTO> ticketWebData = paymentService.getTicketWebData(userId);
            return ResponseEntity.ok(ticketWebData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Transactional
    @GetMapping("/tenants/{tenantId}/payments")
    public ResponseEntity<Page<Payment>> getPaymentsByTenant(
            @PathVariable Long tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<Payment> payments = paymentRepository.findByTenant(tenantId, pageRequest);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
