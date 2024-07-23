package br.com.multiprodutora.ticketeria.gateway;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentIntentRequest {
    private int amount;
    private String currency;
    private String description;
    private String paymentMethodType;


    public PaymentIntentRequest() {
    }

    public PaymentIntentRequest(int amount, String currency, String description, String paymentMethodType) {
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.paymentMethodType = paymentMethodType;

    }


}
