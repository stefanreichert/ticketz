package net.wickedshell.ticketz.service.port.driven.persistence;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import net.wickedshell.ticketz.service.model.Comment;
import net.wickedshell.ticketz.service.model.Ticket;

import java.util.List;

/**
 * Driven port for comment persistence operations.
 */
public interface CommentPersistence {

    /**
     * Find all comments belonging to a ticket.
     *
     * @param ticketNumber the ticket number
     * @return list of comments
     */
    List<Comment> findByTicketNumber(@NotBlank String ticketNumber);

    /**
     * Persist a new comment for a ticket.
     *
     * @param comment the comment to create
     * @param ticket  the ticket the comment belongs to
     * @return the created comment
     */
    Comment create(@Valid Comment comment, @Valid Ticket ticket);
}
