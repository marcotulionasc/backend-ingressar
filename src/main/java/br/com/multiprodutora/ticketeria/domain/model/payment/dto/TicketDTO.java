package br.com.multiprodutora.ticketeria.domain.model.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TicketDTO {
    @JsonProperty("id")
    private String ticketId;
    private String name;
    Double price;
    private Integer quantity;
}
