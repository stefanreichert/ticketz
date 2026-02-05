package net.wickedshell.ticketz.core;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.core.model.Comment;
import net.wickedshell.ticketz.core.model.Ticket;
import net.wickedshell.ticketz.core.port.access.CommentService;
import net.wickedshell.ticketz.core.port.driven.persistence.CommentPersistence;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentPersistence commentPersistence;

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findByTicketNumber(String ticketNumber) {
        return commentPersistence.findByTicketNumber(ticketNumber);
    }

    @Override
    public void create(Comment comment, Ticket ticket) {
        commentPersistence.create(comment, ticket);
    }

}
