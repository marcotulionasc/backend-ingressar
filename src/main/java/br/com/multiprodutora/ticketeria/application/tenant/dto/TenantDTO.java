package br.com.multiprodutora.ticketeria.application.tenant.dto;

import br.com.multiprodutora.ticketeria.domain.Status;

import java.time.LocalDateTime;

public record TenantDTO(
        Long id,

        String name,

        String subdomain,

        String email,

        String password,

        LocalDateTime createdAt,

        Status isTenantActive,

        int activeEventsCount
) {
}
