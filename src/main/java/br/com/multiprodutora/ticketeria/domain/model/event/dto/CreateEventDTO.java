package br.com.multiprodutora.ticketeria.domain.model.event.dto;

import br.com.multiprodutora.ticketeria.domain.model.address.dto.AddressDTO;
import br.com.multiprodutora.ticketeria.domain.model.config.ConfigEvent;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;

public record CreateEventDTO(

        String nameEvent,

        String titleEvent,

        String description,

        String date,

        String local,

        String hourOfStart,

        String hourOfShow,

        String imageEvent,

        String imageFlyer,

        AddressDTO address,

        Tenant tenantId,

        ConfigEvent configEventId


) {

}
