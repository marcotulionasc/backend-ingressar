package br.com.multiprodutora.ticketeria.domain.model.payment;

import br.com.multiprodutora.ticketeria.application.payment.dto.TicketDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PaymentRequest {

    private String eventName;
    private Float ticketPriceTotal;
    private Integer buyQuantity;
    private String userId;
    private String userName;
    private String userEmail;
    private List<TicketDTO> selectedTickets;
    private Long eventId;
    private String tenantId;

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "eventName='" + eventName + '\'' +
                ", ticketPriceTotal=" + ticketPriceTotal +
                ", buyQuantity=" + buyQuantity +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", selectedTickets=" + selectedTickets +
                ", eventId=" + eventId +
                ", tenantId=" + tenantId +
                '}';
    }

}
