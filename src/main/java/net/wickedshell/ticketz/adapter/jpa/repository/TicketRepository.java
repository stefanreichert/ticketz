package net.wickedshell.ticketz.adapter.jpa.repository;

import net.wickedshell.ticketz.adapter.jpa.entity.TicketEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends CrudRepository<TicketEntity, Long> {
    Optional<TicketEntity> findByTicketNumber(String ticketNumber);
}
