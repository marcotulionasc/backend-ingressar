package br.com.multiprodutora.ticketeria.repository;

import br.com.multiprodutora.ticketeria.domain.model.config.ConfigEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigEventRespository extends JpaRepository<ConfigEvent, Long> {


}
