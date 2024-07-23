package br.com.multiprodutora.ticketeria.domain.model.event.dto;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.address.Address;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;

import java.time.LocalDateTime;

public record EventDTO(
        Long id,
        String nameEvent,
        String titleEvent,
        String description,
        String date,
        String local,
        String hourOfStart,
        String hourOfShow,
        LocalDateTime createdAt,
        String imageEvent,
        String imageFlyer,
        Status isEventActive,
        String address
) {

    public EventDTO(Event event) {
        this(
                event.getId(),
                event.getNameEvent(),
                event.getTitleEvent(),
                event.getDescription(),
                event.getDate(),
                event.getLocal(),
                event.getHourOfStart(),
                event.getHourOfShow(),
                event.getCreatedAt(),
                event.getImageEvent(),
                event.getImageFlyer(),
                event.getIsEventActive(),
                event.getAddress().toString()
        );
    }
}


