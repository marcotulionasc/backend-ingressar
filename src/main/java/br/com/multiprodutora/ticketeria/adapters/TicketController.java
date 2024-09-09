package br.com.multiprodutora.ticketeria.adapters;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import br.com.multiprodutora.ticketeria.domain.model.ticket.dto.CreateTicketDTO;
import br.com.multiprodutora.ticketeria.domain.model.ticket.dto.TicketDTO;
import br.com.multiprodutora.ticketeria.repository.EventRepository;
import br.com.multiprodutora.ticketeria.repository.TenantRepository;
import br.com.multiprodutora.ticketeria.repository.TicketRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    @Transactional
    @PostMapping("/tenants/{tenantId}/events/{eventId}/tickets/create")
    public ResponseEntity<Map<String, Object>> createTicket(@PathVariable Long tenantId,
                                                            @PathVariable Long eventId,
                                                            @RequestBody @Valid CreateTicketDTO data,
                                                            UriComponentsBuilder uriBuilder) {
        logger.info("Received request to create ticket for tenantId: {} and eventId: {}", tenantId, eventId);

        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> {
            logger.error("Tenant not found for tenantId: {}", tenantId);
            return new RuntimeException("Tenant not found");
        });

        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            logger.error("Event not found for eventId: {}", eventId);
            return new RuntimeException("Event not found");
        });

        if (!event.getTenant().getId().equals(tenantId)) {
            logger.warn("Permission denied to create ticket for tenantId: {} and eventId: {}", tenantId, eventId);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "You don't have permission to create a ticket for this event.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        var ticket = new Ticket(data);
        ticket.setEvent(event);
        ticket.setTenant(tenant);
        ticketRepository.save(ticket);
        logger.info("Ticket created successfully for tenantId: {} and eventId: {}", tenantId, eventId);

        Map<String, Object> response = new HashMap<>();
        response.put("id", ticket.getId());
        response.put("name", ticket.getNameTicket());
        response.put("eventId", ticket.getEvent().getId());
        response.put("tenantId", ticket.getTenant().getId());

        return ResponseEntity.created(uriBuilder
                .path("/tenants/{tenantId}/events/{eventId}/tickets/{ticketId}")
                .buildAndExpand(
                        tenant.getId(),
                        event.getId(),
                        ticket.getId())
                .toUri())
                .body(response);
    }

    @Transactional
    @GetMapping("/tenants/{tenantId}/events/{eventId}/tickets/{ticketId}")
    public ResponseEntity<TicketDTO> getTicket(@PathVariable Long tenantId,
                                               @PathVariable Long eventId,
                                               @PathVariable Long ticketId) {
        logger.info("Received request to get ticket for tenantId: {}, eventId: {} and ticketId: {}", tenantId, eventId, ticketId);

        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> {
            logger.error("Tenant not found for tenantId: {}", tenantId);
            return new RuntimeException("Tenant not found");
        });

        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            logger.error("Event not found for eventId: {}", eventId);
            return new RuntimeException("Event not found");
        });

        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> {
            logger.error("Ticket not found for ticketId: {}", ticketId);
            return new RuntimeException("Ticket not found");
        });

        TicketDTO ticketDTO = new TicketDTO(
                ticket.getId(),
                ticket.getNameTicket(),
                ticket.getStartDate(),
                ticket.getEndDate(),
                ticket.getIsTicketActive(),
                ticket.getCreatedAt(),
                ticket.getAreaTicket()
        );
        logger.info("Ticket listed successfully for tenantId: {}, eventId: {} and ticketId: {}", tenantId, eventId, ticketId);

        return ResponseEntity.ok(ticketDTO);
    }

    @Transactional
    @GetMapping("/tenants/{tenantId}/events/{eventId}/tickets")
    public ResponseEntity<Iterable<TicketDTO>> getTickets(@PathVariable Long tenantId,
                                                          @PathVariable Long eventId) {
        logger.info("Received request to list tickets for tenantId: {} and eventId: {}", tenantId, eventId);

        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> {
            logger.error("Tenant not found for tenantId: {}", tenantId);
            return new RuntimeException("Tenant not found");
        });

        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            logger.error("Event not found for eventId: {}", eventId);
            return new RuntimeException("Event not found");
        });

        List<TicketDTO> tickets = ticketRepository.findAllByEventAndTenant(event, tenant)
                .stream().map(ticket -> new TicketDTO(
                        ticket.getId(),
                        ticket.getNameTicket(),
                        ticket.getStartDate(),
                        ticket.getEndDate(),
                        ticket.getIsTicketActive(),
                        ticket.getCreatedAt(),
                        ticket.getAreaTicket()
        )).collect(Collectors.toList());
        logger.info("Tickets listed successfully for tenantId: {} and eventId: {}", tenantId, eventId);

        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/tenants/{tenantId}/events/{eventId}/ticketslot")
    public ResponseEntity<List<Map<String, Object>>> getTicketsForCreateLotSpecific(@PathVariable Long tenantId,
                                                                                    @PathVariable Long eventId) {
        logger.info("Received request to list tickets for lot creation for tenantId: {} and eventId: {}", tenantId, eventId);

        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> {
            logger.error("Tenant not found for tenantId: {}", tenantId);
            return new RuntimeException("Tenant not found");
        });

        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            logger.error("Event not found for eventId: {}", eventId);
            return new RuntimeException("Event not found");
        });

        Iterable<Ticket> tickets = ticketRepository.findAllByEventAndTenant(event, tenant);

        List<Map<String, Object>> ticketResponses = new ArrayList<>();
        for (Ticket ticket : tickets) {
            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("id", ticket.getId());
            ticketData.put("nameTicket", ticket.getNameTicket());
            ticketResponses.add(ticketData);
        }
        logger.info("Tickets for lot creation listed successfully for tenantId: {} and eventId: {}", tenantId, eventId);

        return ResponseEntity.ok(ticketResponses);
    }

    @Transactional
    @DeleteMapping("/tenants/{tenantId}/events/{eventId}/tickets/{ticketId}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long tenantId,
                                             @PathVariable Long eventId,
                                             @PathVariable Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> {
            logger.error("Ticket not found for ticketId: {}", ticketId);
            return new RuntimeException("Ticket not found");
        });
        ticketRepository.delete(ticket);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/tenants/{tenantId}/events/{eventId}/ticket/{ticketId}/status")
    public ResponseEntity<Void> updateTicketStatus(@PathVariable Long tenantId,
                                                   @PathVariable Long eventId,
                                                   @PathVariable Long ticketId,
                                                   @RequestBody Status status) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> {
            logger.error("Ticket not found for ticketId: {}", ticketId);
            return new RuntimeException("Ticket not found");
        });
        ticket.setIsTicketActive(status);
        ticketRepository.save(ticket);
        return ResponseEntity.noContent().build();

    }
}
