package net.wickedshell.ticketz.adapter.jpa.repository;

import net.wickedshell.ticketz.adapter.jpa.entity.TicketEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends CrudRepository<TicketEntity, Long> {
    Optional<TicketEntity> findByTicketNumber(String ticketNumber);

    @Query("""
            SELECT t FROM TicketEntity t
            LEFT JOIN t.editor e
            WHERE LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%', :searchText, '%'))
               OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchText, '%'))
               OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchText, '%'))
               OR LOWER(t.author.firstname) LIKE LOWER(CONCAT('%', :searchText, '%'))
               OR LOWER(t.author.lastname) LIKE LOWER(CONCAT('%', :searchText, '%'))
               OR LOWER(e.firstname) LIKE LOWER(CONCAT('%', :searchText, '%'))
               OR LOWER(e.lastname) LIKE LOWER(CONCAT('%', :searchText, '%'))
               OR LOWER(CAST(t.state AS string)) LIKE LOWER(CONCAT('%', :searchText, '%'))
               OR LOWER(t.project.code) LIKE LOWER(CONCAT('%', :searchText, '%'))
               OR LOWER(t.project.name) LIKE LOWER(CONCAT('%', :searchText, '%'))
            """)
    List<TicketEntity> search(@Param("searchText") String searchText);
}
