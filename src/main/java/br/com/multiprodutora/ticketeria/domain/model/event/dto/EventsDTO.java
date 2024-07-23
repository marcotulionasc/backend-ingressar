package br.com.multiprodutora.ticketeria.domain.model.event.dto;

public record EventsDTO(
        long id,
        String nameEvent,
        String date,
        String local,
        String imageEvent

        /* Alinhar os campos com o Estefano
            esses campos são os que estão sendo usados no front-end

         */
) {
}
