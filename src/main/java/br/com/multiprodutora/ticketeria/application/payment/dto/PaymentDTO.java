package br.com.multiprodutora.ticketeria.application.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PaymentDTO {
    @JsonProperty("userId")
    private String userId;

    private String name;
    private String email;
    private String eventName;
    private Long eventId;
    private String tenantId;
    private Double totalAmount;
    private List<TicketDTO> selectedTickets;

    @Override
    public String toString() {
        return "PaymentDTO{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", eventId=" + eventId +
                ", tenantId=" + tenantId +
                ", totalAmount=" + totalAmount +
                ", selectedTickets=" + selectedTickets +
                '}';
    }

}
