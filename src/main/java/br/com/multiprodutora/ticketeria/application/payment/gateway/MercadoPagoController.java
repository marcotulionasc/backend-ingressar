package br.com.multiprodutora.ticketeria.application.payment.gateway;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import br.com.multiprodutora.ticketeria.domain.model.payment.PaymentRequest;
import br.com.multiprodutora.ticketeria.application.payment.dto.PaymentDTO;
import br.com.multiprodutora.ticketeria.application.payment.dto.TicketDTO;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import br.com.multiprodutora.ticketeria.application.event.repository.EventRepository;
import br.com.multiprodutora.ticketeria.application.payment.repository.PaymentRepository;
import br.com.multiprodutora.ticketeria.application.ticket.repository.TicketRepository;
import br.com.multiprodutora.ticketeria.application.user.repository.UserRepository;
import br.com.multiprodutora.ticketeria.application.payment.service.ExternalReferenceService;
import br.com.multiprodutora.ticketeria.application.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.MercadoPago;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.Item;
import com.mercadopago.resources.datastructures.preference.BackUrls;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class MercadoPagoController {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ExternalReferenceService externalReferenceService;

    @Value("${mp.token}")
    private String mercadoPagoAcessToken;

    @Value("${backurl.success}")
    private String backUrlSuccess;

    @Value("${backurl.failure}")
    private String backUrlFailure;

    @Value("${backurl.pending}")
    private String backUrlPending;

    @Value("${mercadopago.notification.url}")
    private String notificationUrl;

    @PostMapping("/create-preference")
    public ResponseEntity<?> createPreference(@RequestBody PaymentRequest paymentRequest) {
        try {
            logger.info("Received payment request for preference creation: {}", paymentRequest.toString());

            if (paymentRequest.getUserId() == null || paymentRequest.getUserId().isEmpty()) {
                logger.error("User ID is null or empty");
                throw new IllegalArgumentException("User ID is null or empty");
            }
            if (paymentRequest.getEventId() == null) {
                logger.error("Event ID is null");
                throw new IllegalArgumentException("Event ID is null");
            }
            if (paymentRequest.getTenantId() == null) {
                logger.error("Tenant ID is null");
                throw new IllegalArgumentException("Tenant ID is null");
            }
            if (paymentRequest.getSelectedTickets() == null || paymentRequest.getSelectedTickets().isEmpty()) {
                logger.error("Selected tickets are null or empty");
                throw new IllegalArgumentException("Selected tickets are null or empty");
            }

            String userId = paymentRequest.getUserId();
            String userName = paymentRequest.getUserName();
            String userEmail = paymentRequest.getUserEmail();

            LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
            String createdAtFormatted = createdAt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            String externalReference = createdAtFormatted + userId;

            MercadoPago.SDK.setAccessToken(mercadoPagoAcessToken);

            Preference preference = new Preference();

            for (TicketDTO selectedTicket : paymentRequest.getSelectedTickets()) {
                Optional<Ticket> ticketOpt = ticketRepository.findById(Long.valueOf(selectedTicket.getTicketId()));

                if (!ticketOpt.isPresent()) {
                    logger.error("Ticket não encontrado para o ID: {}", selectedTicket.getTicketId());
                    throw new IllegalArgumentException("Ticket inválido com ID: " + selectedTicket.getTicketId());
                }

                Ticket ticket = ticketOpt.get();
                Item item = new Item();

                String customTitle = paymentRequest.getEventName() + " - " + ticket.getAreaTicket() + " - " + ticket.getNameTicket();

                try {
                    logger.info("Criando item para o ticket: {}", paymentRequest.getEventName());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                item.setTitle(customTitle)
                        .setQuantity(selectedTicket.getQuantity())
                        .setUnitPrice(selectedTicket.getPrice().floatValue())
                        .setCurrencyId("BRL");

                preference.appendItem(item);
            }

            BackUrls backUrls = new BackUrls();
            backUrls.setSuccess(backUrlSuccess)
                    .setFailure(backUrlFailure)
                    .setPending(backUrlPending);

            preference.setBackUrls(backUrls);
            preference.setAutoReturn(Preference.AutoReturn.approved);
            preference.setExternalReference(externalReference);
            preference.setNotificationUrl(notificationUrl);
            preference.save();

            logger.info("Preference created with ID: [{}] for external reference: [{}]", preference.getId(), externalReference);

            Payment payment = new Payment();
            payment.setId(externalReference);
            payment.setUserId(userId);
            payment.setUserName(userName);
            payment.setUserEmail(userEmail);
            payment.setStatus(Status.PENDING);
            payment.setCreatedAt(createdAt);
            payment.setTotalAmount(Double.valueOf(paymentRequest.getTicketPriceTotal()));
            payment.setEvent(paymentRequest.getEventId());
            payment.setTenant(Long.valueOf(paymentRequest.getTenantId()));
            payment.setIsTicketActive(false);
            payment.setIsTicketsSent(false);

            ObjectMapper objectMapper = new ObjectMapper();
            String selectedTicketsJson = objectMapper.writeValueAsString(paymentRequest.getSelectedTickets());
            payment.setSelectedTicketsJson(selectedTicketsJson);

            paymentRepository.save(payment);

            Map<String, String> response = new HashMap<>();
            response.put("preferenceId", preference.getId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao criar preferência: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar preferência", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Erro interno do servidor"));
        }
    }


    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> savePayment(@RequestBody PaymentDTO paymentDto) {
        logger.info("Received payment data to save: {}", paymentDto);

        Payment savedPayment = paymentService.savePayment(paymentDto);

        logger.info("Payment saved successfully with ID: {}", savedPayment.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("Message: ", "Payment saved successfully!");
        response.put("Payment ID: ", savedPayment.getId());

        return ResponseEntity.ok(response);
    }

//    @PostMapping("/notifications")
//    public ResponseEntity<String> handleNotification(@RequestParam("topic") String topic, @RequestParam("id") String id) {
//
//        if ("payment".equals(topic)) {
//            try {
//                if (id == null || id.isEmpty()) {
//                    logger.error("ID de pagamento inválido.");
//                    return ResponseEntity.ok("ID de pagamento inválido");
//                }
//
//                MercadoPago.SDK.setAccessToken(mercadoPagoAcessToken);
//                com.mercadopago.resources.Payment payment = com.mercadopago.resources.Payment.findById(id);
//
//                if (payment == null) {
//                    logger.error("Pagamento não encontrado para o ID: {}", id);
//                    return ResponseEntity.ok("Pagamento não encontrado");
//                }
//
//                String statusMP = String.valueOf(payment.getStatus());
//                String externalReference = payment.getExternalReference();
//                Float transactionAmount = payment.getTransactionAmount();
//                Double amount = (transactionAmount != null) ? transactionAmount.doubleValue() : 0.0;
//
//                paymentService.updatePaymentStatus(externalReference, statusMP, amount);
//
//                logger.info("Status do pagamento atualizado para referência externa: {} com status: {}", externalReference, statusMP);
//            } catch (Exception e) {
//                logger.error("Erro ao processar a notificação", e);
//
//                return ResponseEntity.ok("Erro ao processar a notificação");
//            }
//        } else {
//            logger.warn("Tópico não suportado ou ID inválido: topic={}, id={}", topic, id);
//
//            return ResponseEntity.ok("Tópico não suportado");
//        }
//
//        return ResponseEntity.ok("Notificação recebida com sucesso");
//    }

}
