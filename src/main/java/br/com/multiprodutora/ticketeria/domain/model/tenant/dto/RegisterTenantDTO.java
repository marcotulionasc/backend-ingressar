package br.com.multiprodutora.ticketeria.domain.model.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterTenantDTO(
        @NotBlank String name,

        @NotBlank String subdomain,

        @NotBlank @Email String email,

        @NotBlank String password
) {
}
