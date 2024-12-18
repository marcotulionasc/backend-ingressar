package br.com.multiprodutora.ticketeria.application.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterTenantDTO(
        @NotBlank String name,

        @NotBlank String subdomain,

        @NotBlank @Email String email,

        @NotBlank String password
) {
}
