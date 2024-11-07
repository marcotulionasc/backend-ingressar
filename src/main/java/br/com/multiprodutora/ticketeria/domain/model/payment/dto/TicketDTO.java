package br.com.multiprodutora.ticketeria.domain.model.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TicketDTO {
    @JsonProperty("ticketId")
    private Long ticketId;

    private Integer quantity;
}
