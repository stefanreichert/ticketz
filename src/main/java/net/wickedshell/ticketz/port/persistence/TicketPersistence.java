package net.wickedshell.ticketz.port.persistence;

import jakarta.validation.Valid;
import net.wickedshell.ticketz.service.model.Ticket;

import java.util.List;

public interface TicketPersistence {
    Ticket loadByTicketNumber(String ticketNumber);

    void delete(String ticketNumber);

    Ticket create(Ticket ticket);

    @Valid Ticket update(@Valid Ticket ticket);

    List<Ticket> findAll();
}
