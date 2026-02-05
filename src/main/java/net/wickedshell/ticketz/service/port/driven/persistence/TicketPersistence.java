package net.wickedshell.ticketz.service.port.driven.persistence;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import net.wickedshell.ticketz.service.model.Ticket;

import java.util.List;

/**
 * Driven port for ticket persistence operations.
 */
public interface TicketPersistence {

    /**
     * Load a ticket by its ticket number.
     *
     * @param ticketNumber the ticket number
     * @return the ticket
     * @throws net.wickedshell.ticketz.service.port.driven.persistence.exception.ObjectNotFoundException if not found
     */
    Ticket loadByTicketNumber(@NotBlank String ticketNumber);

    /**
     * Delete a ticket by its ticket number.
     *
     * @param ticketNumber the ticket number
     * @throws net.wickedshell.ticketz.service.port.driven.persistence.exception.ObjectNotFoundException if not found
     */
    void deleteByTicketNumber(@NotBlank String ticketNumber);

    /**
     * Persist a new ticket.
     *
     * @param ticket the ticket to create
     * @return the created ticket
     */
    Ticket create(@Valid Ticket ticket);

    /**
     * Update an existing ticket.
     *
     * @param ticket the ticket with updated data
     * @return the updated ticket
     * @throws net.wickedshell.ticketz.service.port.driven.persistence.exception.ObjectNotFoundException if not found
     */
    Ticket update(@Valid Ticket ticket);

    /**
     * Find all tickets.
     *
     * @return list of all tickets
     */
    List<Ticket> findAll();

    /**
     * Search tickets by text across multiple fields.
     * Searches: ticketNumber, title, description, author name, editor name, state, project code/name.
     *
     * @param searchText the text to search for (case-insensitive, partial match)
     * @return list of matching tickets
     */
    List<Ticket> search(@NotBlank String searchText);

    /**
     * Get the total number of tickets.
     *
     * @return the ticket count
     */
    long getTicketCount();
}
