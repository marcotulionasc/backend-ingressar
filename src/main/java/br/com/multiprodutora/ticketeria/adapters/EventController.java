package br.com.multiprodutora.ticketeria.adapters;

import br.com.multiprodutora.ticketeria.config.ApiConfig;
import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.event.dto.CreateEventDTO;
import br.com.multiprodutora.ticketeria.domain.model.event.dto.EventDTO;
import br.com.multiprodutora.ticketeria.domain.model.event.dto.EventSummaryDTO;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.repository.EventRepository;
import br.com.multiprodutora.ticketeria.repository.LotRepository;
import br.com.multiprodutora.ticketeria.repository.TenantRepository;
import br.com.multiprodutora.ticketeria.repository.TicketRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private ApiConfig apiConfig;

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    @Transactional
    @PostMapping("/tenants/{tenantId}/events/create")
    public ResponseEntity<Map<String, Object>> createEvent(@PathVariable Long tenantId,
                                                           @RequestBody @Valid CreateEventDTO data,
                                                           UriComponentsBuilder uriBuilder) {
        logger.info("Received request to create event for tenantId: {}", tenantId);
        logger.debug("Event data: {}", data);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
            logger.error("Tenant not found for tenantId: {}", tenantId);
            return new RuntimeException("Tenant not found");
        });

        var event = new Event(data);
        event.setTenant(tenant);
        eventRepository.save(event);
        logger.info("Event created successfully with id: {}", event.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("id", event.getId());
        response.put("nameEvent", event.getNameEvent());
        response.put("titleEvent", event.getTitleEvent());

        return ResponseEntity.created(uriBuilder
                .path("/events/{id}")
                .buildAndExpand(
                        event.getId())
                .toUri()).body(response);
    }

    @Transactional
    @GetMapping("/tenants/{tenantId}/events")
    public ResponseEntity<Iterable<EventDTO>> listEventsByTenant(@PathVariable Long tenantId) {
        logger.info("Received request to list events for tenantId: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
            logger.error("Tenant not found for tenantId: {}", tenantId);
            return new RuntimeException("Tenant not found");
        });

        List<EventDTO> events = tenant.getEvents()
                .stream()
                .map(event -> new EventDTO(
                        event.getId(),
                        event.getNameEvent(),
                        event.getTitleEvent(),
                        event.getDescription(),
                        event.getDate(),
                        event.getLocal(),
                        event.getHourOfStart(),
                        event.getHourOfShow(),
                        event.getCreatedAt(),
                        event.getImageEvent().toString(),
                        event.getImageFlyer().toString(),
                        event.getIsEventActive(),
                        event.getAddress().toString()))
                .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        return new ResponseEntity<>(events, headers, HttpStatus.OK);
    }

    @Transactional
    @GetMapping("/tenants/{tenantId}/events/image")
    public ResponseEntity<Iterable<EventSummaryDTO>> listSummaryEventByTenant(@PathVariable Long tenantId) {
        logger.info("Received request to list summary events for tenantId: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
            logger.error("Tenant not found for tenantId: {}", tenantId);
            return new RuntimeException("Tenant not found");
        });

        List<Event> events = tenant.getEvents();
        List<EventSummaryDTO> eventSummaries = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        for (Event event : events) {
            String base64Image = "";
            if (event.getImageEvent() != null) {
                try {
                    String imageUrl = apiConfig.getApiBaseUrl() + event.getImageEvent();

                    ResponseEntity<byte[]> response = restTemplate.exchange(
                            imageUrl,
                            HttpMethod.GET,
                            null, // No headers not necessary
                            byte[].class
                    );

                    byte[] imageBytes = response.getBody();
                    base64Image = Base64.getEncoder().encodeToString(imageBytes);

                } catch (Exception e) {
                    logger.error("Error fetching image for event id: {}", event.getId(), e);
                    base64Image = "https://via.placeholder.com/300x150.png?text=Imagem+Indisponível";
                }
            } else {
                base64Image = "https://via.placeholder.com/300x150.png?text=Imagem+Indisponível";
            }

            EventSummaryDTO eventSummary = new EventSummaryDTO(
                    event.getId(),
                    event.getTitleEvent(),
                    event.getDate(),
                    event.getIsEventActive(),
                    event.getAddress().getStreet() + ", " +
                            event.getAddress().getNeighborhood() + " - " +
                            event.getAddress().getCity() + " - " +
                            event.getAddress().getUf(),
                    "data:image/jpeg;base64," + base64Image
            );
            eventSummaries.add(eventSummary);
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        return new ResponseEntity<>(eventSummaries, responseHeaders, HttpStatus.OK);
    }

    @Transactional
    @GetMapping("/tenants/{tenantId}/flyers")
    public ResponseEntity<List<String>> getImageFlyers(@PathVariable Long tenantId) {
        logger.info("Received request to list image flyers for tenantId: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
            logger.error("Tenant not found for tenantId: {}", tenantId);
            return new RuntimeException("Tenant not found");
        });

        List<Event> events = tenant.getEvents();
        List<String> imagePaths = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        for (Event event : events) {
            if (event.getImageFlyer() != null) {
                try {

                    String imageUrl = apiConfig.getApiBaseUrl() + event.getImageFlyer();
                    ResponseEntity<byte[]> response = restTemplate.exchange(
                            imageUrl,
                            HttpMethod.GET,
                            null,
                            byte[].class
                    );

                    byte[] imageBytes = response.getBody();
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    imagePaths.add("data:image/jpeg;base64," + base64Image);

                } catch (Exception e) {
                    logger.error("Error fetching flyer image for event id: {}", event.getId(), e);
                    imagePaths.add("https://via.placeholder.com/300x150.png?text=Imagem+Indisponível");
                }
            } else {
                imagePaths.add("https://via.placeholder.com/300x150.png?text=Imagem+Indisponível");
            }
        }
        return ResponseEntity.ok(imagePaths);
    }

    @GetMapping("/tenants/{tenantId}/events/{id}")
    public ResponseEntity<EventDTO> infoAboutEventSpecific(@PathVariable Long id,
                                                           @PathVariable Long tenantId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        EventDTO eventDTO = new EventDTO(event);
        return ResponseEntity.ok(eventDTO);
    }

    @Transactional
    @DeleteMapping("/tenants/{tenantId}/events/{id}/delete")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long tenantId,
                                            @PathVariable Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        eventRepository.delete(event);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/tenants/{tenantId}/events/{id}/status")
    public ResponseEntity<EventDTO> updateEventStatus(@PathVariable Long tenantId,
                                                      @PathVariable Long id,
                                                      @RequestBody Status status) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        event.setIsEventActive(status);
        eventRepository.save(event);
        EventDTO eventDTO = new EventDTO(event);
        return ResponseEntity.ok(eventDTO);
    }

}
