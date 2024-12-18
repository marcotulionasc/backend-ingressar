package br.com.multiprodutora.ticketeria.application.ticket.repository;

import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByTenant(Tenant tenant);

    List<Ticket> findAllByEventAndTenant(Event event, Tenant tenant);

    Iterable<Ticket> findAllByEvent(Event event);

    void deleteAllByEventId(Long id);

    void deleteByTenantId(Long id);

    Collection<Ticket> findAllByEventAndTenantAndAreaTicket(Event event, Tenant tenant, String areaTicket);
}
