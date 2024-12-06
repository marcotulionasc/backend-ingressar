package br.com.multiprodutora.ticketeria.gateway;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import br.com.multiprodutora.ticketeria.domain.model.payment.PaymentRequest;
import br.com.multiprodutora.ticketeria.domain.model.payment.dto.PaymentDTO;
import br.com.multiprodutora.ticketeria.domain.model.payment.dto.TicketDTO;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import br.com.multiprodutora.ticketeria.repository.EventRepository;
import br.com.multiprodutora.ticketeria.repository.PaymentRepository;
import br.com.multiprodutora.ticketeria.repository.TicketRepository;
import br.com.multiprodutora.ticketeria.repository.UserRepository;
import br.com.multiprodutora.ticketeria.service.ExternalReferenceService;
import br.com.multiprodutora.ticketeria.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.Item;
import com.mercadopago.resources.datastructures.preference.BackUrls;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    public ResponseEntity<Map<String, String>> createPreference(@RequestBody PaymentRequest paymentRequest) throws MPException, IOException {
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

        String externalReference = createdAtFormatted + userId; // :TODO the 14 caracters first is about the date and time of the payment

        MercadoPago.SDK.setAccessToken(mercadoPagoAcessToken);

        Preference preference = new Preference();

        for (TicketDTO selectedTicket : paymentRequest.getSelectedTickets()) {
            Optional<Ticket> ticket = ticketRepository.findById(Long.valueOf(selectedTicket.getTicketId()));
            Item item = new Item();

            String customTitle = paymentRequest.getEventName() + " - " + ticket.get().getAreaTicket() + " - " + ticket.get().getNameTicket();

            item.setTitle(customTitle)
                    .setQuantity(selectedTicket.getQuantity())
                    .setUnitPrice(Float.valueOf(ticket.get().getLot().getPriceTicket()))
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

        // String status = externalReferenceService.checkPaymentStatus(externalReference);

        // if ("approved".equalsIgnoreCase(status)) {
           // logger.info("Payment approved for externalReference: [{}]", externalReference);
        // } else {
           // logger.warn("Payment pending or rejected [{}], Status: [{}]",
             //       externalReference, status);
        //}

        Payment payment = new Payment();
        payment.setId(externalReference);
        payment.setUserId(userId);
        payment.setUserName(userName);
        payment.setUserEmail(userEmail);
        payment.setStatus(Status.PENDING);
        payment.setCreatedAt(createdAt);
        payment.setTotalAmount(Double.valueOf(paymentRequest.getTicketPriceTotal()));
        payment.setEventId(paymentRequest.getEventId());
        payment.setTenantId(paymentRequest.getTenantId());
        payment.setIsTicketActive(false); // :TODO when the QR Code is scanned, this field should be updated to true

        ObjectMapper objectMapper = new ObjectMapper();
        String selectedTicketsJson = objectMapper.writeValueAsString(paymentRequest.getSelectedTickets());
        payment.setSelectedTicketsJson(selectedTicketsJson);

        paymentRepository.save(payment);

        Map<String, String> response = new HashMap<>();
        response.put("preferenceId", preference.getId());
        return ResponseEntity.ok(response);
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

    @PostMapping("/notifications")
    public ResponseEntity<String> handleNotification(@RequestParam("topic") String topic, @RequestParam("id") String id) {
        logger.info("Received notification: topic={}, id={}", topic, id);

        if ("payment".equals(topic)) {
            try {
                if (id == null || id.isEmpty()) {
                    logger.error("ID de pagamento inválido.");
                    return ResponseEntity.ok("ID de pagamento inválido");
                }

                MercadoPago.SDK.setAccessToken(mercadoPagoAcessToken);
                com.mercadopago.resources.Payment payment = com.mercadopago.resources.Payment.findById(id);

                if (payment == null) {
                    logger.error("Pagamento não encontrado para o ID: {}", id);
                    return ResponseEntity.ok("Pagamento não encontrado");
                }

                String statusMP = String.valueOf(payment.getStatus());
                String externalReference = payment.getExternalReference();
                Float transactionAmount = payment.getTransactionAmount();
                Double amount = (transactionAmount != null) ? transactionAmount.doubleValue() : 0.0;

                paymentService.updatePaymentStatus(externalReference, statusMP, amount);

                logger.info("Status do pagamento atualizado para referência externa: {} com status: {}", externalReference, statusMP);
            } catch (Exception e) {
                logger.error("Erro ao processar a notificação", e);

                return ResponseEntity.ok("Erro ao processar a notificação");
            }
        } else {
            logger.warn("Tópico não suportado ou ID inválido: topic={}, id={}", topic, id);

            return ResponseEntity.ok("Tópico não suportado");
        }

        return ResponseEntity.ok("Notificação recebida com sucesso");
    }

}
