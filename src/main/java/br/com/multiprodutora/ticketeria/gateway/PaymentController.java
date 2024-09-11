package br.com.multiprodutora.ticketeria.gateway;

import br.com.multiprodutora.ticketeria.domain.model.payment.dto.PaymentRequest;
import br.com.multiprodutora.ticketeria.domain.model.user.User;
import br.com.multiprodutora.ticketeria.repository.PaymentRepository;
import br.com.multiprodutora.ticketeria.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${mercado.pago.access.token}")
    private String mercadoPagoAcessToken;

    private final RestTemplate restTemplate;

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    public PaymentController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequest paymentRequest) {
        try{
            Optional<User> userOptional = userRepository.findByEmail(paymentRequest.email());
            if(userOptional.isEmpty()){
                return ResponseEntity.badRequest().body("User not found");
            }

            User user = userOptional.get();

            logger.info("User object: [{}]", user);

            String idempotencyKey = UUID.randomUUID().toString();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("token", paymentRequest.token());
            requestBody.put("transaction_amount", paymentRequest.transactionAmount());
            requestBody.put("description", paymentRequest.description());
            requestBody.put("installments", paymentRequest.installments());
            requestBody.put("payment_method_id", paymentRequest.paymentMethodId());

            Map<String, String> payer = new HashMap<>();
            payer.put("email", paymentRequest.email());
            requestBody.put("payer", payer);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + mercadoPagoAcessToken);
            headers.set("X-Idempotency-Key", idempotencyKey);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.mercadopago.com/v1/payments",
                    requestBody,
                    Map.class
            );

        }catch (Exception e){
            return ResponseEntity.badRequest().body("Error");
        }
        return ResponseEntity.ok("Success");
    }

}
