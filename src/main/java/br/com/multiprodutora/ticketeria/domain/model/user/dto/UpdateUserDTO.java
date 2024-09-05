package br.com.multiprodutora.ticketeria.domain.model.user.dto;

import br.com.multiprodutora.ticketeria.domain.model.address.Address;

public record UpdateUserDTO(
        String name,
        String birthDate,
        String email,
        String password,
        String cpf,
        String phone,
        String imageProfileBase64,
        Address address
) {

}
