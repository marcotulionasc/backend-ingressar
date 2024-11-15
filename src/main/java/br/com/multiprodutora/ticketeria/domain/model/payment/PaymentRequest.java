package br.com.multiprodutora.ticketeria.domain.model.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {


    private String eventName;
    private Float ticketPriceTotal;
    private Integer buyQuantity;
    private String userId;

}
