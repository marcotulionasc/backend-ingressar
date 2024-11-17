package br.com.multiprodutora.ticketeria.service;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import br.com.multiprodutora.ticketeria.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    public boolean updateStatus(Long ticketId, Status status) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
            if (ticket != null) {
                ticket.setIsTicketActive(status);
                ticketRepository.save(ticket);
                return true;
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
        return false;
    }
}

