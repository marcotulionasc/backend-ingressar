package br.com.multiprodutora.ticketeria.domain.model.config;

import br.com.multiprodutora.ticketeria.config.enums.Decision;
import br.com.multiprodutora.ticketeria.application.configevent.dto.CreateConfigEventDTO;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "ConfigEvent")
@Table(name = "config_event")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")

public class ConfigEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private int limitBuyTicket;

    private Decision writeTicketNominalWhenBuy;

    private Decision buyTicketInSiteOrNot;

    private Decision buyOnSiteSellOccultThoughtLinkSpecific;

    private Genero genero;

    private int classificationOfEvent; // classificação do evento

    private String googleAnalyticsCode;

    private String idPixelFacebook;

    private String descriptionEvent;

    private String releaseNotesOfArtist;

    private String textThatAppearsOnTheTicket;

    private String textThatAppearsOnTheTicketWhenGoToEmail;

    private String linkVideoOfYoutube;

    private String imageOfSeatMap;

    private Decision imageFlyerShowInHome;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    public ConfigEvent (CreateConfigEventDTO data){
        this.limitBuyTicket = data.limitBuyTicket();
        this.writeTicketNominalWhenBuy = data.writeTicketNominalWhenBuy();
        this.buyTicketInSiteOrNot = data.buyTicketInSiteOrNot();
        this.buyOnSiteSellOccultThoughtLinkSpecific = data.buyOnSiteSellOccultThoughtLinkSpecific();
        this.genero = data.genero();
        this.classificationOfEvent = data.classificationOfEvent();
        this.googleAnalyticsCode = data.googleAnalyticsCode();
        this.idPixelFacebook = data.idPixelFacebook();
        this.descriptionEvent = data.descriptionEvent();
        this.releaseNotesOfArtist = data.releaseNotesOfArtist();
        this.textThatAppearsOnTheTicket = data.textThatAppearsOnTheTicket();
        this.textThatAppearsOnTheTicketWhenGoToEmail = data.textThatAppearsOnTheTicketWhenGoToEmail();
        this.linkVideoOfYoutube = data.linkVideoOfYoutube();
        this.imageOfSeatMap = data.imageOfSeatMap();
        this.imageFlyerShowInHome = data.imageFlyerShowInHome();
        this.event = data.idEvent();
        this.tenant = data.tenantId();
    }
}
