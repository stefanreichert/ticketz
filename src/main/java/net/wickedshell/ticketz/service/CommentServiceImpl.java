package net.wickedshell.ticketz.service;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.service.model.Comment;
import net.wickedshell.ticketz.service.model.Ticket;
import net.wickedshell.ticketz.service.port.access.CommentService;
import net.wickedshell.ticketz.service.port.persistence.CommentPersistence;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentPersistence commentPersistence;

    @Override
    public List<Comment> findByTicketNumber(String ticketNumber) {
        return commentPersistence.findByTicketNumber(ticketNumber);
    }

    @Override
    public void create(Comment comment, Ticket ticket) {
        commentPersistence.create(comment, ticket);
    }

}
