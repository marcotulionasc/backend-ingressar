package br.com.multiprodutora.ticketeria.application.address.dto;

import br.com.multiprodutora.ticketeria.domain.model.address.UF;

public record AddressDTO(

        String street,

        String neighborhood,

        String numberAddress,

        String cep,

        UF uf,

        String complement,

        String city
) {



}
