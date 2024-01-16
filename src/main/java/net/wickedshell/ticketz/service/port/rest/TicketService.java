package net.wickedshell.ticketz.service.port.rest;

import net.wickedshell.ticketz.service.model.Ticket;

import java.util.List;

public interface TicketService {
    Ticket loadByTicketNumber(String ticketNumber);

    Ticket create(Ticket ticket);

    Ticket update(Ticket ticket);

    List<Ticket> findAll();
}
