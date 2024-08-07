package br.com.multiprodutora.ticketeria.domain.model.tenant.dto;

import br.com.multiprodutora.ticketeria.domain.Status;

import java.time.LocalDateTime;

public record TenantsDTO(
        Long id,

        String name,

        String subdomain,

        LocalDateTime createdAt,

        Status isTenantActive

) {
}
