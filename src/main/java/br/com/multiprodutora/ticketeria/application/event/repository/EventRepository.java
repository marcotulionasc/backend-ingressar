package br.com.multiprodutora.ticketeria.application.event.repository;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByTenantId(Long tenantId);

    Event findByTenantIdAndId(Long tenantId, Long id);

    Event findByIsEventActive(Status status);

    void deleteByTenantId(Long id);

    Event findByNameEvent(String nameEvent);

    Event findByTicketsId(Long ticketId);

}
