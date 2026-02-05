package net.wickedshell.ticketz.service.port.access;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import net.wickedshell.ticketz.service.model.Comment;
import net.wickedshell.ticketz.service.model.Ticket;

import java.util.List;

/**
 * Access port for ticket management operations.
 */
public interface TicketService {

    /**
     * Load a ticket by its unique ticket number.
     *
     * @param ticketNumber the ticket number
     * @return the ticket with possible next states populated
     */
    Ticket loadByTicketNumber(@NotBlank String ticketNumber);

    /**
     * Delete a ticket by its ticket number. The ticket's project must be active.
     *
     * @param ticketNumber the ticket number
     */
    void deleteByTicketNumber(@NotBlank String ticketNumber);

    /**
     * Create a new ticket. The ticket number, state, and author are assigned automatically.
     *
     * @param ticket the ticket to create
     * @return the created ticket
     */
    Ticket create(@Valid Ticket ticket);

    /**
     * Update an existing ticket. Validates state transitions and editor assignment.
     *
     * @param ticket the ticket with updated data
     * @return the updated ticket with possible next states populated
     */
    Ticket update(@Valid Ticket ticket);

    /**
     * Update an existing ticket and add a comment in a single transaction.
     *
     * @param ticket  the ticket with updated data
     * @param comment the comment to add
     * @return the updated ticket with possible next states populated
     */
    Ticket updateWithComment(@Valid Ticket ticket, @Valid Comment comment);

    /**
     * List all tickets.
     *
     * @return list of all tickets with possible next states populated
     */
    List<Ticket> findAll();

    /**
     * Search tickets by text across multiple fields.
     * Returns all tickets if searchText is null or blank.
     *
     * @param searchText the text to search for (case-insensitive, partial match)
     * @return list of matching tickets with possible next states populated
     */
    List<Ticket> search(String searchText);

    /**
     * Evaluate whether the current user can edit the given ticket based on state, project
     * status, and user role (author/editor).
     *
     * @param ticket the ticket to evaluate
     * @return true if the current user can edit the ticket
     */
    boolean evaluateCanBeEdited(@Valid Ticket ticket);
}
