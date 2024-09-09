package br.com.multiprodutora.ticketeria.adapters;

import br.com.multiprodutora.ticketeria.domain.model.config.ConfigEvent;
import br.com.multiprodutora.ticketeria.domain.model.config.dto.CreateConfigEventDTO;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.repository.ConfigEventRespository;
import br.com.multiprodutora.ticketeria.repository.EventRepository;
import br.com.multiprodutora.ticketeria.repository.TenantRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api")
public class ConfigEventController {

    @Autowired
    private ConfigEventRespository configEventRespository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Transactional
    @PostMapping("/tenants/{tenantId}/events/{eventId}/config/create")
    public ResponseEntity<CreateConfigEventDTO> createConfigOfEvent(@PathVariable long tenantId,
                                                                    @PathVariable long eventId,
                                                                    @RequestBody CreateConfigEventDTO data,
                                                                    UriComponentsBuilder uriBuilder) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        var configEvent = new ConfigEvent(data);
        configEvent.setEvent(event);
        configEvent.setTenant(tenant);

        configEventRespository.save(configEvent);

        event.setConfigEvent(configEvent);
        eventRepository.save(event);

        return ResponseEntity.created(uriBuilder
                .path("/api/tenants/{tenantId}/events/{eventId}/config/{configId}")
                .buildAndExpand(
                        tenantId,
                        eventId,
                        configEvent.getId())
                .toUri()).body(data);
    }

    @GetMapping("/tenants/{tenantId}/events/{eventId}/config")
    public ResponseEntity<CreateConfigEventDTO> getConfigOfEvent(@PathVariable long tenantId,
                                                                 @PathVariable long eventId) {
        ConfigEvent configEvent = configEventRespository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("ConfigEvent not found"));

        CreateConfigEventDTO data = new CreateConfigEventDTO(
                configEvent.getLimitBuyTicket(),
                configEvent.getWriteTicketNominalWhenBuy(),
                configEvent.getBuyTicketInSiteOrNot(),
                configEvent.getBuyOnSiteSellOccultThoughtLinkSpecific(),
                configEvent.getGenero(),
                configEvent.getClassificationOfEvent(),
                configEvent.getGoogleAnalyticsCode(),
                configEvent.getIdPixelFacebook(),
                configEvent.getDescriptionEvent(),
                configEvent.getReleaseNotesOfArtist(),
                configEvent.getTextThatAppearsOnTheTicket(),
                configEvent.getTextThatAppearsOnTheTicketWhenGoToEmail(),
                configEvent.getLinkVideoOfYoutube(),
                configEvent.getImageOfSeatMap(),
                configEvent.getImageFlyerShowInHome(),
                configEvent.getEvent(),
                configEvent.getTenant()
        );

        return ResponseEntity.ok(data);
    }

}
