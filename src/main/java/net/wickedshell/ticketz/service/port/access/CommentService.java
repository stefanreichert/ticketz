package net.wickedshell.ticketz.service.port.access;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import net.wickedshell.ticketz.service.model.Comment;
import net.wickedshell.ticketz.service.model.Ticket;

import java.util.List;

/**
 * Access port for comment management operations.
 */
public interface CommentService {

    /**
     * Find all comments belonging to a ticket.
     *
     * @param ticketNumber the ticket number
     * @return list of comments ordered by creation date
     */
    List<Comment> findByTicketNumber(@NotBlank String ticketNumber);

    /**
     * Create a new comment for a ticket.
     *
     * @param comment the comment to create
     * @param ticket  the ticket the comment belongs to
     */
    void create(@Valid Comment comment, @Valid Ticket ticket);
}
