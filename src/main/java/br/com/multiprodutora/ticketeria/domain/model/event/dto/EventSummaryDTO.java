package br.com.multiprodutora.ticketeria.domain.model.event.dto;

import br.com.multiprodutora.ticketeria.domain.Status;

public record EventSummaryDTO(
        Long id,
        String titleEvent,
        String date,
        Status isEventActive,
        String address,
        String base64Image
        ) {}
