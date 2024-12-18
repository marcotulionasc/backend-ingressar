package br.com.multiprodutora.ticketeria.application.configevent.dto;

import br.com.multiprodutora.ticketeria.config.Decision;
import br.com.multiprodutora.ticketeria.domain.model.config.Genero;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;

public record CreateConfigEventDTO(
        int limitBuyTicket,

        Decision writeTicketNominalWhenBuy,

        Decision buyTicketInSiteOrNot,

        Decision buyOnSiteSellOccultThoughtLinkSpecific,

        Genero genero,

        int classificationOfEvent,

        String googleAnalyticsCode,

        String idPixelFacebook,

        String descriptionEvent,

        String releaseNotesOfArtist,

        String textThatAppearsOnTheTicket,

        String textThatAppearsOnTheTicketWhenGoToEmail,

        String linkVideoOfYoutube,

        String imageOfSeatMap,

        Decision imageFlyerShowInHome,

        Event idEvent,

        Tenant tenantId

) {
}
