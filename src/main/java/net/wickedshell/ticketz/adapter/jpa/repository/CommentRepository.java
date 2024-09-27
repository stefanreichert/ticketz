package net.wickedshell.ticketz.adapter.jpa.repository;

import net.wickedshell.ticketz.adapter.jpa.entity.CommentEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommentRepository extends CrudRepository<CommentEntity, Long> {
    @Query("SELECT comment FROM CommentEntity comment WHERE comment.ticket.ticketNumber = :ticketNumber")
    List<CommentEntity> findByTicketNumber(String ticketNumber);
}
