package br.com.multiprodutora.ticketeria.application.user.dto;


public record CreateUserDTO(
        String name,
        String birthDate,
        String email,
        String password,
        String cpf,
        String phone,
        String imageProfileBase64
) {
}
