package br.com.multiprodutora.ticketeria.gateway;

import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import br.com.multiprodutora.ticketeria.domain.model.payment.PaymentRequest;
import br.com.multiprodutora.ticketeria.domain.model.payment.dto.PaymentDTO;
import br.com.multiprodutora.ticketeria.service.PaymentService;
import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPConfException;
import com.mercadopago.exceptions.MPException;
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
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class MercadoPagoController {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoController.class);

    @Autowired
    private PaymentService paymentService;

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
    public String createPreference(@RequestBody PaymentRequest paymentRequest) throws MPException {
        logger.info("Received payment request for preference creation: " + paymentRequest.toString());
        String userId = paymentRequest.getUserId();

        LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));

        String createdAtFormatted = createdAt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String externalReference = userId + "_" + createdAtFormatted;

        MercadoPago.SDK.setAccessToken(mercadoPagoAcessToken);

        Preference preference = new Preference();

        Item item = new Item();
        item.setTitle(paymentRequest.getEventName())
                .setQuantity(paymentRequest.getBuyQuantity())
                .setUnitPrice(paymentRequest.getTicketPriceTotal())
                .setCurrencyId("BRL");

        preference.appendItem(item);

        BackUrls backUrls = new BackUrls();
        backUrls.setSuccess(backUrlSuccess)
                .setFailure(backUrlFailure)
                .setPending(backUrlPending);

        preference.setBackUrls(backUrls);
        preference.setAutoReturn(Preference.AutoReturn.valueOf("approved"));
        preference.setExternalReference(externalReference);
        preference.setNotificationUrl(notificationUrl);
        preference.save();

        logger.info("Preference created with ID: " + preference.getId() + " for external reference: " + externalReference);

        return preference.getId();
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> savePayment(@RequestBody PaymentDTO paymentDto) {
        logger.info("Received payment data to save: " + paymentDto);

        Payment savedPayment = paymentService.savePayment(paymentDto);

        logger.info("Payment saved successfully with ID: " + savedPayment.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("Message: ", "Pagamento salvo com sucesso");
        response.put("Payment ID: ", savedPayment.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/notifications")
    public ResponseEntity<String> handleNotification(@RequestBody Map<String, Object> notificationData){
        logger.info("Received notification data: " + notificationData);

        String topic = (String) notificationData.get("topic"); // Pode ser payment
        String id = (String) notificationData.get("id");

        if("payment".equals(topic)){
            try{
                MercadoPago.SDK.setAccessToken(mercadoPagoAcessToken);
                com.mercadopago.resources.Payment payment = com.mercadopago.resources.Payment.findById(id);

                String statusMP = String.valueOf(payment.getStatus());
                String externalReference = payment.getExternalReference();
                Double amount = Double.valueOf(payment.getTransactionAmount());

                paymentService.updatePaymentStatus(externalReference, statusMP, amount);

                logger.info("Payment status updated for external reference: " + externalReference + " with status: " + statusMP);
            } catch (Exception e) {
                logger.error("Erro ao recuperar informações do pagamento", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a notificação");
            }
        }
        return ResponseEntity.ok("Notificação recebida com sucesso");
    }

}
