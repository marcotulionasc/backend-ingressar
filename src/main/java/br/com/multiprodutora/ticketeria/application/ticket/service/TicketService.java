package br.com.multiprodutora.ticketeria.application.ticket.service;

import br.com.multiprodutora.ticketeria.domain.Status;
import br.com.multiprodutora.ticketeria.domain.model.ticket.Ticket;
import br.com.multiprodutora.ticketeria.domain.model.validation.TicketValidationLog;
import br.com.multiprodutora.ticketeria.application.ticket.repository.TicketRepository;
import br.com.multiprodutora.ticketeria.application.ticket.repository.TicketValidationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketValidationLogRepository logRepository;

    public boolean updateStatus(Long ticketId, Status status) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);

            if (ticket == null) {
                logger.warn("Ticket não encontrado para o ID: {}", ticketId);
                return false;
            }

            ticket.setIsTicketActive(status);
            ticketRepository.save(ticket);

            TicketValidationLog log = new TicketValidationLog(ticketId, status.toString());
            logRepository.save(log);

            logger.info("Status do ticket atualizado e log de validação criado para o ticket ID: {}", ticketId);
            return true;
        } catch (Exception e) {
            logger.error("Erro ao atualizar o status do ticket ID: {}", ticketId, e);
            return false;
        }
    }
}
