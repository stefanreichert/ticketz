package net.wickedshell.ticketz.service.port.persistence;

import jakarta.validation.Valid;
import net.wickedshell.ticketz.service.model.Ticket;

import java.util.List;

public interface TicketPersistence {
    Ticket loadByTicketNumber(String ticketNumber);

    Ticket create(Ticket ticket);

    @Valid Ticket update(@Valid Ticket ticket);

    List<Ticket> findAll();

    long getTicketCount();
}
