package br.com.multiprodutora.ticketeria.adapters;

import br.com.multiprodutora.ticketeria.gateway.PaymentIntentRequest;
import br.com.multiprodutora.ticketeria.service.StripePaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    @Autowired
    private StripePaymentService stripePaymentService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<String> createPaymentIntent(@RequestBody PaymentIntentRequest paymentIntentRequest) throws StripeException {
        PaymentIntent paymentIntent = stripePaymentService.createPaymentIntent(
                paymentIntentRequest.getAmount(),
                paymentIntentRequest.getCurrency(),
                paymentIntentRequest.getDescription(),
                paymentIntentRequest.getPaymentMethodType()
        );

        String paymentIntentJson = paymentIntent.toJson();

        return ResponseEntity.ok().body(paymentIntentJson);
    }

}

