package br.com.multiprodutora.ticketeria.gateway;

import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import br.com.multiprodutora.ticketeria.domain.model.payment.PaymentRequest;
import br.com.multiprodutora.ticketeria.domain.model.payment.dto.PaymentDTO;
import br.com.multiprodutora.ticketeria.service.PaymentService;
import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.Item;
import com.mercadopago.resources.datastructures.preference.BackUrls;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class MercadoPagoController {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoController.class);

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-preference")
    public String createPreference(@RequestBody PaymentRequest paymentRequest) throws MPException {
        logger.info("Received payment request for preference creation: " + paymentRequest);

        MercadoPago.SDK.setAccessToken("APP_USR-8935049088704556-091209-5cb0774e6b3b2c2a3075a992ae117395-254412224");

        Preference preference = new Preference();

        Item item = new Item();
        item.setTitle(paymentRequest.getEventName())
                .setQuantity(paymentRequest.getBuyQuantity())
                .setUnitPrice(paymentRequest.getTicketPriceTotal())
                .setCurrencyId("BRL");

        preference.appendItem(item);

        BackUrls backUrls = new BackUrls();
        backUrls.setSuccess("http://ingressonaingressar.vercel.app/success.html")
                .setFailure("http://ingressonaingressar.vercel.app/failure.html")
                .setPending("http://ingressonaingressar.vercel.app/pending.html");

        preference.setBackUrls(backUrls);
        preference.setAutoReturn(Preference.AutoReturn.valueOf("approved"));
        preference.save();

        String userId = paymentRequest.getUserId();

        logger.info("Preference created with ID: " + preference.getId() + " for user ID: " + userId);

        return preference.getId();
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> savePayment(@RequestBody PaymentDTO paymentDto) {
        logger.info("Received payment data to save: " + paymentDto);

        Payment savedPayment = paymentService.savePayment(paymentDto);

        logger.info("Payment saved successfully with ID: " + savedPayment.getId());

        // Retornar uma resposta simples sem o objeto Payment completo
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Pagamento salvo com sucesso");
        response.put("paymentId", savedPayment.getId());

        return ResponseEntity.ok(response);
    }

}
