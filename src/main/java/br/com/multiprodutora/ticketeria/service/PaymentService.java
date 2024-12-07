package br.com.multiprodutora.ticketeria.service;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.config.ConfigEvent;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.lot.Lot;
import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import br.com.multiprodutora.ticketeria.domain.model.payment.dto.PaymentDTO;
import br.com.multiprodutora.ticketeria.domain.model.payment.dto.TicketDTO;
import br.com.multiprodutora.ticketeria.domain.model.payment.dto.TicketPDFDTO;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import br.com.multiprodutora.ticketeria.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

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
    private ConfigEventRespository configEventRepository;

    @Autowired
    private LotRepository lotRepository;

    private final Logger logger = Logger.getLogger(PaymentService.class.getName());

    public Payment savePayment(PaymentDTO paymentDto) {
        Payment payment = new Payment();
        payment.setUserId(paymentDto.getUserId());
        payment.setUserName(paymentDto.getName());
        payment.setUserEmail(paymentDto.getEmail());
        payment.setTotalAmount(paymentDto.getTotalAmount());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setStatus(Status.PENDING);
        payment.setIsTicketActive(true);

        Event event = eventRepository.findById(paymentDto.getEventId()).orElse(null);
        if (event != null) {
            payment.setEvent(event.getId());
        } else {
            throw new IllegalArgumentException("Evento não encontrado com ID: " + paymentDto.getEventId());
        }

        Tenant tenant = tenantRepository.findById(event.getTenant().getId()).orElse(null);
        if (tenant != null) {
            payment.setTenant(tenant.getId());
        } else {
            throw new IllegalArgumentException("Tenant não encontrado com ID: " + paymentDto.getTenantId());
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String selectedTicketsJson = objectMapper.writeValueAsString(paymentDto.getSelectedTickets());
            payment.setSelectedTicketsJson(selectedTicketsJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar selectedTickets", e);
        }

        return paymentRepository.save(payment);
    }

    public List<TicketPDFDTO> getTicketPDFData(String paymentId) throws Exception {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new Exception("Pagamento não encontrado"));

        ObjectMapper objectMapper = new ObjectMapper();
        List<TicketDTO> selectedTickets = objectMapper.readValue(
                payment.getSelectedTicketsJson(),
                new TypeReference<List<TicketDTO>>() {});

        Long eventId = payment.getEvent();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new Exception("Evento não encontrado com ID: " + eventId));
        Optional<ConfigEvent> configEvent = configEventRepository.findByEventId(eventId);

        List<TicketPDFDTO> ticketPDFDTOList = new ArrayList<>();
        for (TicketDTO selectedTicket : selectedTickets) {
            Ticket ticket = ticketRepository.findById(Long.valueOf(selectedTicket.getTicketId()))
                    .orElseThrow(() -> new Exception("Ingresso não encontrado"));

            Lot activeLot = lotRepository.findActiveLotByTicketId(ticket.getId())
                    .orElseThrow(() -> new Exception("Lote ativo não encontrado"));

            TicketPDFDTO ticketPDFDTO = new TicketPDFDTO(
                    event.getTitleEvent(),                        // nomeEvento
                    event.getDate(),                              // dataEvento
                    event.getHourOfStart(),                       // aberturaPortas
                    event.getLocal(),                             // localEvento
                    event.getAddress().toString(),                // enderecoEvento
                    ticket.getId(),                               // idIngresso
                    ticket.getNameTicket(),                       // nomeIngresso
                    ticket.getAreaTicket(),                       // areaIngresso
                    activeLot.getId(),                            // idLoteAtivo
                    activeLot.getPriceTicket(),                   // valorLote
                    activeLot.getAmountTicket(),                  // quantidadeLote
                    activeLot.getTaxPriceTicket(),                // taxaLote
                    payment.getCreatedAt().toString(),            // dataCompra
                    payment.getUserName(),                        // nomeComprador
                    configEvent.get().getTextThatAppearsOnTheTicketWhenGoToEmail()); // textoNoIngresso

            ticketPDFDTOList.add(ticketPDFDTO);
        }

        return ticketPDFDTOList;
    }

    public List<TicketPDFDTO> getTicketWebData(Long userId) throws Exception {

        var userIdString = userId.toString();
        List<Payment> payments = paymentRepository.findByUserId(userIdString);

        if (payments.isEmpty()) {
            return new ArrayList<>();
        }

        List<TicketPDFDTO> allTickets = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (Payment payment : payments) {

            List<TicketDTO> selectedTickets = objectMapper.readValue(
                    payment.getSelectedTicketsJson(),
                    new TypeReference<List<TicketDTO>>() {});

            Long eventId = payment.getEvent();
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new Exception("Evento não encontrado com ID: " + eventId));
            Optional<ConfigEvent> configEvent = configEventRepository.findByEventId(eventId);

            if (!configEvent.isPresent()) {
                throw new Exception("Configuração do Evento não encontrada para o evento: " + configEvent.get().getEvent());
            }

            for (TicketDTO selectedTicket : selectedTickets) {
                Ticket ticket = ticketRepository.findById(Long.valueOf(selectedTicket.getTicketId()))
                        .orElseThrow(() -> new Exception("Ingresso não encontrado"));

                Lot activeLot = lotRepository.findActiveLotByTicketId(ticket.getId())
                        .orElseThrow(() -> new Exception("Lote ativo não encontrado"));

                TicketPDFDTO ticketPDFDTO = new TicketPDFDTO(
                        event.getTitleEvent(),                        // nomeEvento
                        event.getDate(),                              // dataEvento
                        event.getHourOfStart(),                       // aberturaPortas
                        event.getLocal(),                             // localEvento
                        event.getAddress().toString(),                // enderecoEvento
                        ticket.getId(),                               // idIngresso
                        ticket.getNameTicket(),                       // nomeIngresso
                        ticket.getAreaTicket(),                       // areaIngresso
                        activeLot.getId(),                            // idLoteAtivo
                        activeLot.getPriceTicket(),                   // valorLote
                        activeLot.getAmountTicket(),                  // quantidadeLote
                        activeLot.getTaxPriceTicket(),                // taxaLote
                        payment.getCreatedAt().toString(),            // dataCompra
                        payment.getUserName(),                        // nomeComprador
                        configEvent.get().getTextThatAppearsOnTheTicketWhenGoToEmail() // textoNoIngresso
                );

                allTickets.add(ticketPDFDTO);
            }
        }

        return allTickets;
    }

    public void updatePaymentStatus(String externalReference, String status, Double amount) {
        if (externalReference == null || externalReference.isEmpty()) {
            throw new IllegalArgumentException("Referência externa inválida. Deve ser um valor não nulo e não vazio.");
        }

        Payment payment = paymentRepository.findById(externalReference)
                .orElseThrow(() -> new IllegalArgumentException("Pagamento não encontrado com externalReference: " + externalReference));

        if (status == null || status.isEmpty()) {
            throw new IllegalArgumentException("Status inválido. Deve ser um valor não nulo e não vazio.");
        }

        Status paymentStatus;
        switch (status.toLowerCase()) {
            case "approved":
                paymentStatus = Status.APPROVED;
                break;
            case "pending":
                paymentStatus = Status.PENDING;
                break;
            case "in_process":
                paymentStatus = Status.IN_PROCESS;
                break;
            case "rejected":
                paymentStatus = Status.REJECTED;
                break;
            default:
                paymentStatus = Status.UNKNOWN;
                break;
        }

        payment.setStatus(paymentStatus);
        payment.setTotalAmount(amount != null ? amount : 0.0);
        paymentRepository.save(payment);

        if (payment.getStatus() == Status.APPROVED) {
            logger.info("Pagamento aprovado processado com sucesso para ID: " + externalReference);
        }


    }

}

