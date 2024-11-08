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
import java.util.List;
import java.util.Optional;

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

    public List<TicketPDFDTO> getTicketPDFData(Long paymentId) throws Exception {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new Exception("Pagamento não encontrado"));

        // Desserializar selectedTicketsJson
        ObjectMapper objectMapper = new ObjectMapper();
        List<TicketDTO> selectedTickets = objectMapper.readValue(
                payment.getSelectedTicketsJson(),
                new TypeReference<List<TicketDTO>>() {});

        Event event = payment.getEvent();
        Optional<ConfigEvent> configEvent = configEventRepository.findByEventId(event.getId());

        // Para cada ingresso selecionado, montar o DTO
        List<TicketPDFDTO> ticketPDFDTOList = new ArrayList<>();
        for (TicketDTO selectedTicket : selectedTickets) {
            Ticket ticket = ticketRepository.findById(selectedTicket.getTicketId())
                    .orElseThrow(() -> new Exception("Ingresso não encontrado"));

            // Obter o lote ativo associado ao ingresso
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

    // Novo método para Web
    public List<TicketPDFDTO> getTicketWebData(Long userId) throws Exception {

        var userIdString = userId.toString();
        List<Payment> payments = paymentRepository.findByUserId(userIdString);

        if (payments.isEmpty()) {
            return new ArrayList<>(); // Retorna lista vazia se não houver pagamentos
        }

        List<TicketPDFDTO> allTickets = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (Payment payment : payments) {
            // Desserializar selectedTicketsJson
            List<TicketDTO> selectedTickets = objectMapper.readValue(
                    payment.getSelectedTicketsJson(),
                    new TypeReference<List<TicketDTO>>() {});

            Event event = payment.getEvent();
            Optional<ConfigEvent> configEvent = configEventRepository.findByEventId(event.getId());

            if (!configEvent.isPresent()) {
                throw new Exception("Configuração do Evento não encontrada para o evento: " + event.getTitleEvent());
            }

            for (TicketDTO selectedTicket : selectedTickets) {
                Ticket ticket = ticketRepository.findById(selectedTicket.getTicketId())
                        .orElseThrow(() -> new Exception("Ingresso não encontrado"));

                // Obter o lote ativo associado ao ingresso
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
}

