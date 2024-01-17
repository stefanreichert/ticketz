package net.wickedshell.ticketz.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.service.exception.ValidationException;
import net.wickedshell.ticketz.service.model.Ticket;
import net.wickedshell.ticketz.service.model.TicketState;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.persistence.TicketPersistence;
import net.wickedshell.ticketz.service.port.rest.TicketService;
import net.wickedshell.ticketz.service.port.rest.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static net.wickedshell.ticketz.service.model.TicketState.*;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketPersistence ticketPersistence;
    private final UserService userService;

    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    public Ticket loadByTicketNumber(String ticketNumber) {
        return ticketPersistence.loadByTicketNumber(ticketNumber);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    public Ticket create(Ticket ticket) {
        ticket.setTicketNumber(UUID.randomUUID().toString().substring(0, 5));
        ticket.setState(CREATED);
        ticket.setAuthor(userService.getPricipalUser());
        return ticketPersistence.create(ticket);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    public Ticket update(Ticket ticket) {
        Ticket existingTicket = ticketPersistence.loadByTicketNumber(ticket.getTicketNumber());
        if (existingTicket.getState() != ticket.getState()) {
            validateStateChange(existingTicket, ticket);
            changeEditorIfRequired(ticket);
        } else {
            // keep editor since it must not change
            ticket.setEditor(existingTicket.getEditor());
        }
        ticket.setAuthor(existingTicket.getAuthor());
        return ticketPersistence.update(ticket);
    }

    private void changeEditorIfRequired(Ticket ticket) {
        TicketState newState = ticket.getState();
        if (newState == IN_PROGRESS) {
            ticket.setEditor(userService.getPricipalUser());
        } else if (newState == CLOSED || newState == REOPENED) {
            ticket.setEditor(null);
        }
    }

    private void validateStateChange(Ticket existingTicket, Ticket ticket) {
        TicketState newState = ticket.getState();
        if (!existingTicket.getState().checkIsPermittedSuccessor(newState)) {
            throw new ValidationException(
                    String.format("Invalid new state: given state transition is not permitted - %s -> %s.",
                            existingTicket.getState(), newState));
        }
        if (newState == CLOSED || newState == REOPENED) {
            User currentUser = userService.getPricipalUser();
            String authorEmail = existingTicket.getAuthor().getEmail();
            if (!authorEmail.equals(currentUser.getEmail())) {
                throw new ValidationException(
                        "Invalid new state: closing or reopening a ticket is only allowed by the author.");
            }
        }
    }

    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<Ticket> findAll() {
        return ticketPersistence.findAll();
    }
}
