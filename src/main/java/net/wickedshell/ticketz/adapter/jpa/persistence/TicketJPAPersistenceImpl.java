package net.wickedshell.ticketz.adapter.jpa.persistence;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.jpa.entity.TicketEntity;
import net.wickedshell.ticketz.adapter.jpa.repository.TicketRepository;
import net.wickedshell.ticketz.core.model.Ticket;
import net.wickedshell.ticketz.core.port.driven.persistence.TicketPersistence;
import net.wickedshell.ticketz.core.port.driven.persistence.exception.ObjectNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.StreamSupport;

@Component
@Validated
@RequiredArgsConstructor
public class TicketJPAPersistenceImpl implements TicketPersistence {

    private static final String TICKET_NOT_FOUND = "Ticket not found: %s";

    @Qualifier("jpaModelMapper")
    private final ModelMapper mapper;
    private final TicketRepository ticketRepository;

    @Override
    public Ticket loadByTicketNumber(String ticketNumber) {
        TicketEntity ticketEntity = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ObjectNotFoundException(String.format(TICKET_NOT_FOUND, ticketNumber)));
        return mapper.map(ticketEntity, Ticket.class);
    }

    @Override
    public void deleteByTicketNumber(String ticketNumber) {
        TicketEntity ticketEntity = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ObjectNotFoundException(String.format(TICKET_NOT_FOUND, ticketNumber)));
        ticketRepository.delete(ticketEntity);
    }

    @Override
    public Ticket create(Ticket ticket) {
        TicketEntity ticketEntity = new TicketEntity();
        mapper.map(ticket, ticketEntity);
        return mapper.map(ticketRepository.save(ticketEntity), Ticket.class);
    }

    @Override
    public Ticket update(Ticket ticket) {
        TicketEntity ticketEntity = ticketRepository.findByTicketNumber(ticket.getTicketNumber())
                .orElseThrow(() -> new ObjectNotFoundException(String.format(TICKET_NOT_FOUND, ticket.getTicketNumber())));
        validateVersion(ticketEntity, ticket);
        mapper.map(ticket, ticketEntity);
        return mapper.map(ticketRepository.save(ticketEntity), Ticket.class);
    }

    @Override
    public List<Ticket> findAll() {
        return StreamSupport.stream(ticketRepository.findAll().spliterator(), false)
                .map(ticketEntity -> mapper.map(ticketEntity, Ticket.class))
                .toList();
    }

    @Override
    public List<Ticket> search(String searchText) {
        return ticketRepository.search(searchText).stream()
                .map(ticketEntity -> mapper.map(ticketEntity, Ticket.class))
                .toList();
    }

    @Override
    public long getTicketCount() {
        return ticketRepository.count();
    }

    private void validateVersion(TicketEntity ticketEntity, Ticket ticket) {
        if (ticket.getVersion() != ticketEntity.getVersion()) {
            throw new OptimisticLockException("Staled ticket data for update");
        }
    }
}
