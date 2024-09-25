package net.wickedshell.ticketz.adapter.jpa.persistence;

import jakarta.persistence.OptimisticLockException;
import net.wickedshell.ticketz.adapter.jpa.converter.UserToUserEntityConverter;
import net.wickedshell.ticketz.adapter.jpa.entity.TicketEntity;
import net.wickedshell.ticketz.adapter.jpa.repository.TicketRepository;
import net.wickedshell.ticketz.service.model.Ticket;
import net.wickedshell.ticketz.service.port.persistence.TicketPersistence;
import net.wickedshell.ticketz.service.port.persistence.exception.ObjectNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.StreamSupport;

@Component
public class TicketJPAPersistenceImpl implements TicketPersistence {

    private final ModelMapper mapper;
    private final TicketRepository ticketRepository;

    public TicketJPAPersistenceImpl(TicketRepository ticketRepository, UserToUserEntityConverter userConverter) {
        this.ticketRepository = ticketRepository;
        mapper = new ModelMapper();
        mapper.addConverter(userConverter);
    }

    @Override
    public Ticket loadByTicketNumber(String ticketNumber) {
        TicketEntity ticketEntity = ticketRepository.findByTicketNumber(ticketNumber).orElseThrow(ObjectNotFoundException::new);
        return mapper.map(ticketEntity, Ticket.class);
    }

    @Override
    public void deleteByTicketNumber(String ticketNumber) {
        TicketEntity ticketEntity = ticketRepository.findByTicketNumber(ticketNumber).orElseThrow(ObjectNotFoundException::new);
        ticketRepository.delete(ticketEntity);
    }

    @Override
    public Ticket create(Ticket ticket) {
        TicketEntity ticketEntity = mapper.map(ticket, TicketEntity.class);
        return mapper.map(ticketRepository.save(ticketEntity), Ticket.class);
    }

    @Override
    public Ticket update(Ticket ticket) {
        TicketEntity ticketEntity = ticketRepository.findByTicketNumber(ticket.getTicketNumber()).orElseThrow(ObjectNotFoundException::new);
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
    public long getTicketCount() {
        return ticketRepository.count();
    }

    private void validateVersion(TicketEntity ticketEntity, Ticket ticket) {
        if (ticket.getVersion() != ticketEntity.getVersion()) {
            throw new OptimisticLockException("Staled ticket data for update");
        }
    }
}
