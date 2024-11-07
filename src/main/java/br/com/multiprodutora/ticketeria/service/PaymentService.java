package br.com.multiprodutora.ticketeria.service;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import br.com.multiprodutora.ticketeria.domain.model.payment.dto.PaymentDTO;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        // Buscar o evento pelo ID
        Event event = eventRepository.findById(paymentDto.getEventId()).orElse(null);
        if (event != null) {
            payment.setEvent(event);
        } else {
            throw new IllegalArgumentException("Evento não encontrado com ID: " + paymentDto.getEventId());
        }

        // Obter o tenant
        Tenant tenant = tenantRepository.findById(event.getTenant().getId()).orElse(null);
        if (tenant != null) {
            payment.setTenant(tenant);
        } else {
            throw new IllegalArgumentException("Tenant não encontrado com ID: " + paymentDto.getTenantId());
        }

        // Serializar selectedTickets em JSON e armazenar no campo selectedTicketsJson
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String selectedTicketsJson = objectMapper.writeValueAsString(paymentDto.getSelectedTickets());
            payment.setSelectedTicketsJson(selectedTicketsJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar selectedTickets", e);
        }

        return paymentRepository.save(payment);
    }

}
