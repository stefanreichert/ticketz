package net.wickedshell.ticketz.service;

import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.service.model.Comment;
import net.wickedshell.ticketz.service.model.Ticket;
import net.wickedshell.ticketz.service.port.access.CommentService;
import net.wickedshell.ticketz.service.port.driven.persistence.CommentPersistence;

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
    public void create(@Valid Comment comment, @Valid Ticket ticket) {
        commentPersistence.create(comment, ticket);
    }

}
