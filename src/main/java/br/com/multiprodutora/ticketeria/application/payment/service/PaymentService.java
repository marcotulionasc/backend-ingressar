package br.com.multiprodutora.ticketeria.application.payment.service;

import br.com.multiprodutora.ticketeria.application.configevent.repository.ConfigEventRespository;
import br.com.multiprodutora.ticketeria.application.event.repository.EventRepository;
import br.com.multiprodutora.ticketeria.application.lot.repository.LotRepository;
import br.com.multiprodutora.ticketeria.application.payment.repository.PaymentRepository;
import br.com.multiprodutora.ticketeria.application.tenant.repository.TenantRepository;
import br.com.multiprodutora.ticketeria.application.ticket.repository.TicketRepository;
import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.config.ConfigEvent;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.lot.Lot;
import br.com.multiprodutora.ticketeria.domain.model.payment.Payment;
import br.com.multiprodutora.ticketeria.application.payment.dto.PaymentDTO;
import br.com.multiprodutora.ticketeria.application.payment.dto.TicketDTO;
import br.com.multiprodutora.ticketeria.application.payment.dto.TicketPDFDTO;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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

    public List<TicketPDFDTO> getTicketWebData(Long userId) {
        var userIdString = userId.toString();
        List<Payment> payments = paymentRepository.findByUserId(userIdString);

        if (payments.isEmpty()) {
            return Collections.emptyList();
        }

        List<TicketPDFDTO> allTickets = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (Payment payment : payments) {
            try {
                List<TicketDTO> selectedTickets = objectMapper.readValue(
                        payment.getSelectedTicketsJson(),
                        new TypeReference<List<TicketDTO>>() {});

                Long eventId = payment.getEvent();
                Event event = eventRepository.findById(eventId)
                        .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado com ID: " + eventId));
                ConfigEvent configEvent = configEventRepository.findByEventId(eventId)
                        .orElseThrow(() -> new IllegalArgumentException("Configuração do Evento não encontrada para o evento: " + eventId));

                for (TicketDTO selectedTicket : selectedTickets) {
                    Ticket ticket = ticketRepository.findById(Long.valueOf(selectedTicket.getTicketId()))
                            .orElseThrow(() -> new IllegalArgumentException("Ingresso não encontrado com ID: " + selectedTicket.getTicketId()));

                    Lot activeLot = lotRepository.findActiveLotByTicketId(ticket.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Lote ativo não encontrado para o ingresso com ID: " + ticket.getId()));

                    // Validar valores antes de criar o DTO
                    String enderecoEvento = event.getAddress() != null ? event.getAddress().toString() : "Endereço não informado";

                    TicketPDFDTO ticketPDFDTO = new TicketPDFDTO(
                            event.getTitleEvent(),                        // nomeEvento
                            event.getDate(),                              // dataEvento
                            event.getHourOfStart(),                       // aberturaPortas
                            event.getLocal(),                             // localEvento
                            enderecoEvento,                               // enderecoEvento
                            ticket.getId(),                               // idIngresso
                            ticket.getNameTicket(),                       // nomeIngresso
                            ticket.getAreaTicket(),                       // areaIngresso
                            activeLot.getId(),                            // idLoteAtivo
                            activeLot.getPriceTicket(),                   // valorLote
                            activeLot.getAmountTicket(),                  // quantidadeLote
                            activeLot.getTaxPriceTicket(),                // taxaLote
                            payment.getCreatedAt().toString(),            // dataCompra
                            payment.getUserName(),                        // nomeComprador
                            configEvent.getTextThatAppearsOnTheTicketWhenGoToEmail() // textoNoIngresso
                    );

                    allTickets.add(ticketPDFDTO);
                }
            } catch (Exception e) {
                // Logar o erro e continuar processando os próximos pagamentos
                System.err.println("Erro ao processar pagamento ID: " + payment.getId() + ". " + e.getMessage());
                e.printStackTrace();
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

