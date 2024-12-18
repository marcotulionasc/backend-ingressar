package br.com.multiprodutora.ticketeria.application.lot.controller;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.lot.Lot;
import br.com.multiprodutora.ticketeria.application.lot.dto.CreateLotDTO;
import br.com.multiprodutora.ticketeria.application.lot.dto.LotDTO;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import br.com.multiprodutora.ticketeria.application.event.repository.EventRepository;
import br.com.multiprodutora.ticketeria.application.lot.repository.LotRepository;
import br.com.multiprodutora.ticketeria.application.tenant.repository.TenantRepository;
import br.com.multiprodutora.ticketeria.application.ticket.repository.TicketRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api")
public class LotController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Transactional
    @PostMapping("/tenants/{tenantId}/events/{eventId}/ticket/{ticketId}/lot/create")
    public ResponseEntity<CreateLotDTO> createLot(@PathVariable Long tenantId,
                                                  @PathVariable Long eventId,
                                                  @PathVariable Long ticketId,
                                                  @RequestBody CreateLotDTO data,
                                                  UriComponentsBuilder uriBuilder) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        Lot lot = new Lot(data);
        lot.setEvent(event);
        lot.setTenant(tenant);
        lot.setTicket(ticket);

        lot = lotRepository.save(lot);

        ticket.setLot(lot);
        ticketRepository.save(ticket);

        CreateLotDTO responseDto = new CreateLotDTO(lot);

        return ResponseEntity.created(uriBuilder
                .path("/tenants/{tenantId}/events/{eventId}/tickets/{ticketId}/lots/{lotId}")
                .buildAndExpand(
                        tenantId,
                        eventId,
                        ticketId,
                        lot.getId())
                .toUri()).body(responseDto);
    }

    @GetMapping("/tenants/{tenantId}/events/{eventId}/tickets/{ticketId}/lots/{lotId}")
    public ResponseEntity<Lot> getLot(@PathVariable Long tenantId,
                                      @PathVariable Long eventId,
                                      @PathVariable Long ticketId,
                                      @PathVariable Long lotId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        Lot lot = lotRepository.findById(lotId).orElseThrow(() -> new RuntimeException("Lot not found"));

        if (lot.getEvent().getId() != event.getId() ||
                lot.getTenant().getId() != tenant.getId() ||
                lot.getTicket().getId() != ticket.getId()) {
            throw new RuntimeException("Lot not found");
        }

        return ResponseEntity.ok(lot);

    }

    @Transactional
    @GetMapping("/tenants/{tenantId}/events/{eventId}/tickets/{ticketId}/lots")
    public ResponseEntity<List<LotDTO>> getLots(@PathVariable Long tenantId,
                                                @PathVariable Long eventId,
                                                @PathVariable Long ticketId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        Iterable<Lot> lotsIterable = lotRepository.findAllByEventAndTenantAndTicket(
                event,
                tenant,
                ticket
        );

        List<LotDTO> lotDTOs = StreamSupport
                .stream(lotsIterable.spliterator(), false)
                .map(lot -> new LotDTO(
                        lot.getId(),
                        lot.getNameLot(),
                        lot.getPriceTicket(),
                        lot.getAmountTicket(),
                        lot.getTaxPriceTicket(),
                        lot.getOrderLot(),
                        lot.getIsLotActive()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(lotDTOs);
    }

    @PutMapping("/tenants/{tenantId}/events/{eventId}/tickets/{ticketId}/lots/{lotId}")
    @Transactional
    public ResponseEntity<CreateLotDTO> updateLot(@PathVariable Long tenantId,
                                                  @PathVariable Long eventId,
                                                  @PathVariable Long ticketId,
                                                  @PathVariable Long lotId,
                                                  CreateLotDTO data) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot not found"));

        lot.setNameLot(data.nameLot());
        lot.setPriceTicket(data.priceTicket());
        lot.setAmountTicket(data.amountTicket());
        lot.setTaxPriceTicket(data.taxPriceTicket());
        lot.setOrderLot(data.orderLot());
        lot.setEvent(event);
        lot.setTenant(tenant);
        lot.setTicket(ticket);

        lotRepository.save(lot);

        return ResponseEntity.ok(data);

    }

    @PutMapping("/tenants/{tenantId}/events/{eventId}/tickets/{ticketId}/lots/{lotId}/active")
    @Transactional
    public ResponseEntity<CreateLotDTO> activeStatusLot(@PathVariable Long tenantId,
                                                        @PathVariable Long eventId,
                                                        @PathVariable Long ticketId,
                                                        @PathVariable Long lotId,
                                                        @Valid CreateLotDTO data) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot not found"));

        lot.setIsLotActive(Status.ACTIVE);

        lotRepository.save(lot);

        return ResponseEntity.ok(data);

    }

    @PutMapping("/tenants/{tenantId}/events/{eventId}/tickets/{ticketId}/lots/{lotId}/inactive")
    @Transactional
    public ResponseEntity<CreateLotDTO> inactiveStatusLot(@PathVariable Long tenantId,
                                                          @PathVariable Long eventId,
                                                          @PathVariable Long ticketId,
                                                          @PathVariable Long lotId,
                                                          @Valid CreateLotDTO data) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot not found"));

        lot.setIsLotActive(Status.INACTIVE);

        lotRepository.save(lot);

        return ResponseEntity.ok(data);

    }

    @DeleteMapping("/tenants/{tenantId}/events/{eventId}/tickets/{ticketId}/lots/{lotId}")
    @Transactional
    public ResponseEntity<CreateLotDTO> deleteLot(@PathVariable Long tenantId,
                                                  @PathVariable Long eventId,
                                                  @PathVariable Long ticketId,
                                                  @PathVariable Long lotId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot not found"));

        if (lot.getEvent().getId() != event.getId() ||
                lot.getTenant().getId() != tenant.getId() ||
                lot.getTicket().getId() != ticket.getId()) {
            throw new RuntimeException("Lot not found");
        }

        lotRepository.delete(lot);

        return ResponseEntity.ok().build();

    }
}
