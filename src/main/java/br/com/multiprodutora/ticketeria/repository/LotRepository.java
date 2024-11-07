package br.com.multiprodutora.ticketeria.repository;

import br.com.multiprodutora.ticketeria.domain.model.event.Event;
import br.com.multiprodutora.ticketeria.domain.model.lot.Lot;
import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface LotRepository extends JpaRepository<Lot, Long> {

    Iterable<Lot> findAllByEventAndTenantAndTicket(Event event,
                                                   Tenant tenant,
                                                   Ticket ticket);

    Iterable<Lot> findAllByEvent(Event event);

    void deleteAllByEventId(Long id);

    void deleteByTenantId(Long id);

    @Query("SELECT l FROM Lot l WHERE l.ticket.id = :ticketId AND l.isLotActive = br.com.multiprodutora.ticketeria.domain.Status.ACTIVE")
    Optional<Lot> findActiveLotByTicketId(Long ticketId);

}
