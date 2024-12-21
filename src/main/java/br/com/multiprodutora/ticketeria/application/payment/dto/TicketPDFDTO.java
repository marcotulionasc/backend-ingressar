package br.com.multiprodutora.ticketeria.application.payment.dto;

import br.com.multiprodutora.ticketeria.domain.model.address.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TicketPDFDTO {

    private String nameEvent;
    private String titleEvent;
    private String date;
    private String local;
    private Address address;
    private String ticketId;
    private String ticketName;
    private Double ticketPrice;
    private Integer ticketQuantity;
    private String paidAt;
    private String userName;
    private String userEmail;

}
