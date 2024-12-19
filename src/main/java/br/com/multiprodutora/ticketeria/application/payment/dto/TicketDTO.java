package br.com.multiprodutora.ticketeria.application.payment.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TicketDTO {

    @JsonAlias({"id", "ticketId"}) //:TODO isso aqui é gambiarra, mas o front-end está mandando o id do ingresso com o nome de ticketId
    private String ticketId;
    private String name;
    Double price;
    private Integer quantity;
}
