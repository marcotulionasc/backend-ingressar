package br.com.multiprodutora.ticketeria.domain.model.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {


    private String eventName;
    private Float ticketPrice;
    private Integer quantity;
    private String userId;

}
