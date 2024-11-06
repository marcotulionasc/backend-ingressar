package br.com.multiprodutora.ticketeria.domain.model.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketDTO {
    @JsonProperty("ticketId")
    private Long ticketId;

    @JsonProperty("lotId")
    private Long lotId;

    private Integer quantity;
}
