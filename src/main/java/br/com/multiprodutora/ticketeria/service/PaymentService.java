package br.com.multiprodutora.ticketeria.service;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.lot.Lot;
import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import br.com.multiprodutora.ticketeria.domain.model.payment.dto.PaymentDTO;
import br.com.multiprodutora.ticketeria.domain.model.payment.dto.TicketDTO;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import br.com.multiprodutora.ticketeria.repository.EventRepository;
import br.com.multiprodutora.ticketeria.repository.LotRepository;
import br.com.multiprodutora.ticketeria.repository.PaymentRepository;
import br.com.multiprodutora.ticketeria.repository.TicketRepository;
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
        payment.setPaymentStatus(Status.PENDING);
        payment.setIsTicketActive(true);

        Event event = eventRepository.findByNameEvent(paymentDto.getEventName());
        payment.setEvent(event);

        List<Ticket> tickets = new ArrayList<>();
        for (TicketDTO ticketDto : paymentDto.getSelectedTickets()) {

            // **Add Null Checks for ticketId and lotId**
            if (ticketDto.getTicketId() == null) {
                throw new IllegalArgumentException("Ticket ID cannot be null");
            }
            if (ticketDto.getLotId() == null) {
                throw new IllegalArgumentException("Lot ID cannot be null");
            }

            Ticket ticket = ticketRepository.findById(ticketDto.getTicketId()).orElse(null);
            if (ticket != null) {
                Lot lot = lotRepository.findById(ticketDto.getLotId()).orElse(null);
                if (lot != null) {
                    ticket.setLot(lot);
                    ticket.setEvent(event);
                    ticket.setIsTicketActive(Status.ACTIVE);
                    ticket.setCreatedAt(LocalDateTime.now());
                    tickets.add(ticket);
                } else {
                    // Handle the case where lot is null
                    throw new IllegalArgumentException("Lot not found with ID: " + ticketDto.getLotId());
                }
            } else {
                // Handle the case where ticket is null
                throw new IllegalArgumentException("Ticket not found with ID: " + ticketDto.getTicketId());
            }
        }
        payment.setTickets(tickets);

        return paymentRepository.save(payment);
    }
}
