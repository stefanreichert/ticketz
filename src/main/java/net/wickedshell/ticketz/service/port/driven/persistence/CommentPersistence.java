package net.wickedshell.ticketz.service.port.driven.persistence;

import net.wickedshell.ticketz.service.model.Comment;
import net.wickedshell.ticketz.service.model.Ticket;

import java.util.List;

public interface CommentPersistence {

    List<Comment> findByTicketNumber(String ticketNumber);

    Comment create(Comment comment, Ticket ticket);
}
