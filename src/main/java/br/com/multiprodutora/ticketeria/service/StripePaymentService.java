package br.com.multiprodutora.ticketeria.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class StripePaymentService {

    @Value("${stripe.secretKey}")
    private String stripeSecretKey;

    public PaymentIntent createPaymentIntent(int amount, String currency, String description, String paymentMethodType) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        PaymentIntentCreateParams params = new PaymentIntentCreateParams.Builder()
                .setAmount((long) amount)
                .setCurrency(currency)
                .setDescription(description)
                .putMetadata("payment_method_type", paymentMethodType)
                .build();

        return PaymentIntent.create(params);
    }
}
