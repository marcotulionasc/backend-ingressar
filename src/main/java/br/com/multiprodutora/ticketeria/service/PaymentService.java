package br.com.multiprodutora.ticketeria.service;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.payment.PaymentTicket;
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

        // Buscar o evento pelo ID (certifique-se de que o PaymentDTO tem o campo eventId)
        Event event = eventRepository.findById(paymentDto.getEventId()).orElse(null);
        if (event != null) {
            payment.setEvent(event);
        } else {
            throw new IllegalArgumentException("Evento não encontrado com ID: " + paymentDto.getEventId());
        }

        // Obter o tenant
        Tenant tenant = tenantRepository.findById(event.getTenant().getId()).orElse(null);
        payment.setTenant(tenant);

        // Criar a lista de PaymentTickets
        List<PaymentTicket> paymentTickets = new ArrayList<>();
        for (TicketDTO ticketDto : paymentDto.getSelectedTickets()) {

            if (ticketDto.getTicketId() == null) {
                throw new IllegalArgumentException("Ticket ID cannot be null");
            }

            Ticket ticket = ticketRepository.findById(ticketDto.getTicketId()).orElse(null);
            if (ticket != null) {
                PaymentTicket paymentTicket = new PaymentTicket();
                paymentTicket.setTicket(ticket);
                paymentTicket.setPayment(payment);
                paymentTicket.setQuantity(ticketDto.getQuantity());

                paymentTickets.add(paymentTicket);
            } else {
                throw new IllegalArgumentException("Ticket não encontrado com ID: " + ticketDto.getTicketId());
            }
        }
        payment.setPaymentTickets(paymentTickets);

        return paymentRepository.save(payment);
    }

}
