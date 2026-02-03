package net.wickedshell.ticketz.adapter.jpa.persistence;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.jpa.entity.CommentEntity;
import net.wickedshell.ticketz.adapter.jpa.repository.CommentRepository;
import net.wickedshell.ticketz.adapter.jpa.repository.TicketRepository;
import net.wickedshell.ticketz.service.model.Comment;
import net.wickedshell.ticketz.service.model.Ticket;
import net.wickedshell.ticketz.service.port.driven.persistence.CommentPersistence;
import net.wickedshell.ticketz.service.port.driven.persistence.exception.ObjectNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommentJPAPersistenceImpl implements CommentPersistence {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    @Qualifier("jpaModelMapper")
    private final ModelMapper mapper;

    @Override
    public List<Comment> findByTicketNumber(String ticketNumber) {
        List<CommentEntity> commentEntities = commentRepository.findByTicketNumber(ticketNumber);
        return commentEntities.stream().map(commentEntity -> mapper.map(commentEntity, Comment.class)).toList();
    }

    @Override
    public Comment create(Comment comment, Ticket ticket) {
        CommentEntity commentEntity = new CommentEntity();
        mapper.map(comment, commentEntity);
        commentEntity.setTicket(ticketRepository.findByTicketNumber(ticket.getTicketNumber()).orElseThrow(ObjectNotFoundException::new));
        return mapper.map(commentRepository.save(commentEntity), Comment.class);
    }
}
