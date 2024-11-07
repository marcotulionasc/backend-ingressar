package br.com.multiprodutora.ticketeria.service;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.lot.Lot;
import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import br.com.multiprodutora.ticketeria.domain.model.payment.dto.PaymentDTO;
import br.com.multiprodutora.ticketeria.domain.model.payment.dto.TicketDTO;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import br.com.multiprodutora.ticketeria.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private LotRepository lotRepository;

    public Payment savePayment(PaymentDTO paymentDto) {
        Payment payment = new Payment();
        payment.setUserId(paymentDto.getUserId());
        payment.setUserName(paymentDto.getName());
        payment.setUserEmail(paymentDto.getEmail());
        payment.setTotalAmount(paymentDto.getTotalAmount());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setPaymentStatus(Status.ACTIVE);
        payment.setIsTicketActive(true);

        Event event = eventRepository.findByNameEvent(paymentDto.getEventName());
        payment.setEvent(event);

        Tenant tenant = tenantRepository.findById(event.getTenant().getId()).orElse(null);
        payment.setTenant(tenant);

        List<Ticket> tickets = new ArrayList<>();
        for (TicketDTO ticketDto : paymentDto.getSelectedTickets()) {

            // **Add Null Checks for ticketId and lotId**
            if (ticketDto.getTicketId() == null) {
                throw new IllegalArgumentException("Ticket ID cannot be null");
            }

            Ticket ticket = ticketRepository.findById(ticketDto.getTicketId()).orElse(null);
        }
        payment.setTickets(tickets);

        return paymentRepository.save(payment);
    }
}
