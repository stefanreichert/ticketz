package net.wickedshell.ticketz.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.service.model.Ticket;
import net.wickedshell.ticketz.port.persistence.TicketPersistence;
import net.wickedshell.ticketz.service.exception.ServiceException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static net.wickedshell.ticketz.service.model.TicketState.CREATED;

@Service
@Transactional
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class TicketService {

    private final TicketPersistence ticketPersistence;

    public Ticket loadByTicketNumber(String ticketNumber) {
        return ticketPersistence.loadByTicketNumber(ticketNumber);
    }

    public void delete(String ticketNumber) {
        ticketPersistence.delete(ticketNumber);
    }

    public Ticket create(Ticket ticket) {
        ticket.setTicketNumber(UUID.randomUUID().toString().substring(0, 5));
        ticket.setState(CREATED);
        ticket.setTicketNumber(UUID.randomUUID().toString());
        return ticketPersistence.create(ticket);
    }

    public Ticket update(Ticket ticket) {
        Ticket existingTicket = ticketPersistence.loadByTicketNumber(ticket.getTicketNumber());
        if (!existingTicket.getState().checkIsPermittedSuccessor(ticket.getState())) {
            throw new ServiceException(String.format("Invalid state: %s is not permitted a successor of %s", ticket.getState(), existingTicket.getState()));
        }
        return ticketPersistence.update(ticket);
    }

    public List<Ticket> findAll() {
        return ticketPersistence.findAll();
    }
}
