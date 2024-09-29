package net.wickedshell.ticketz.service.port.access;

import net.wickedshell.ticketz.service.model.Comment;
import net.wickedshell.ticketz.service.model.Ticket;

import java.util.List;

public interface TicketService {
    Ticket loadByTicketNumber(String ticketNumber);

    void deleteByTicketNumber(String ticketNumber);

    Ticket create(Ticket ticket);

    Ticket update(Ticket ticket);

    Ticket update(Ticket ticket, Comment comment);

    List<Ticket> findAll();

    boolean evaluateCanBeEdited(Ticket ticket);

}
