package net.wickedshell.ticketz.service.port.driven.persistence;

import jakarta.validation.Valid;
import net.wickedshell.ticketz.service.model.Ticket;

import java.util.List;

public interface TicketPersistence {
    Ticket loadByTicketNumber(String ticketNumber);

    void deleteByTicketNumber(String ticketNumber);

    Ticket create(Ticket ticket);

    @Valid Ticket update(@Valid Ticket ticket);

    List<Ticket> findAll();

    long getTicketCount();

}
