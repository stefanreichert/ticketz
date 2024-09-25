package net.wickedshell.ticketz.service.port.rest;

import net.wickedshell.ticketz.service.model.Ticket;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface TicketService {
    Ticket loadByTicketNumber(String ticketNumber);

    void deleteByTicketNumber(String ticketNumber);

    Ticket create(Ticket ticket);

    Ticket update(Ticket ticket);

    List<Ticket> findAll();

    @PreAuthorize("hasRole('ROLE_USER')")
    boolean evaluateCanBeEdited(Ticket ticket);

}
