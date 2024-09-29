package net.wickedshell.ticketz.service.port.access;

import net.wickedshell.ticketz.service.model.Comment;
import net.wickedshell.ticketz.service.model.Ticket;

import java.util.List;

public interface CommentService {

    List<Comment> findByTicketNumber(String ticketNumber);

    void create(Comment comment, Ticket ticket);
}