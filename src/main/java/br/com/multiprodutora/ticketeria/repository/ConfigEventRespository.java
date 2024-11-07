package br.com.multiprodutora.ticketeria.repository;

import br.com.multiprodutora.ticketeria.domain.model.config.ConfigEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigEventRespository extends JpaRepository<ConfigEvent, Long> {

    Optional<ConfigEvent> findByEventId(Long eventId);
}
