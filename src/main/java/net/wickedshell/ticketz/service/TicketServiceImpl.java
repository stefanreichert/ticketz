package net.wickedshell.ticketz.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
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
import java.util.Set;

import static net.wickedshell.ticketz.service.model.TicketState.*;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private static final String TICKET_NUMBER_TEMPLATE = "TICKETZ-%d";
    private final TicketPersistence ticketPersistence;
    private final UserService userService;

    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    public Ticket loadByTicketNumber(String ticketNumber) {
        Ticket ticket = ticketPersistence.loadByTicketNumber(ticketNumber);
        updatePossibleNextStates(ticket);
        return ticket;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    public Ticket create(@Valid Ticket ticket) {
        long nextTicketNumber = ticketPersistence.getTicketCount() + 1;
        ticket.setTicketNumber(String.format(TICKET_NUMBER_TEMPLATE, nextTicketNumber));
        ticket.setState(CREATED);
        ticket.setAuthor(userService.getCurrentUser());
        return ticketPersistence.create(ticket);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    public Ticket update(@Valid Ticket ticket) {
        Ticket existingTicket = ticketPersistence.loadByTicketNumber(ticket.getTicketNumber());
        if (!evaluateCanBeEdited(existingTicket)) {
            throw new ValidationException(
                    String.format("Invalid action for state: ticket is closed and cannot be edited: %s.",
                            existingTicket.getTicketNumber()));
        }
        if (existingTicket.getState() != ticket.getState()) {
            validateStateChange(existingTicket, ticket);
            changeEditorIfRequired(ticket);
        } else {
            // keep editor since it must not change
            ticket.setEditor(existingTicket.getEditor());
        }
        ticket.setAuthor(existingTicket.getAuthor());
        Ticket updatedTicket = ticketPersistence.update(ticket);
        updatePossibleNextStates(updatedTicket);
        return updatedTicket;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<Ticket> findAll() {
        List<Ticket> tickets = ticketPersistence.findAll();
        tickets.forEach(this::updatePossibleNextStates);
        return tickets;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    public boolean evaluateCanBeEdited(Ticket ticket) {
        if (ticket.getState() == CLOSED) {
            return false;
        }
        if (ticket.getState() == IN_PROGRESS) {
            return ticket.getEditor().getEmail().equals(userService.getCurrentUser().getEmail());
        }
        return true;
    }

    private void changeEditorIfRequired(Ticket ticket) {
        TicketState newState = ticket.getState();
        if (newState == IN_PROGRESS) {
            ticket.setEditor(userService.getCurrentUser());
        } else if (newState == CLOSED || newState == REOPENED) {
            ticket.setEditor(null);
        }
    }

    private void validateStateChange(Ticket existingTicket, Ticket ticket) {
        TicketState newState = ticket.getState();
        if (!existingTicket.getState().checkIsPermittedSuccessor(newState)) {
            throw new ValidationException(String.format("Invalid new state: given state transition is not permitted - %s -> %s.", existingTicket.getState(), newState));
        }
        if (newState == CLOSED || newState == REOPENED) {
            User currentUser = userService.getCurrentUser();
            String authorEmail = existingTicket.getAuthor().getEmail();
            if (!authorEmail.equals(currentUser.getEmail())) {
                throw new ValidationException("Invalid new state: closing or reopening a ticket is only allowed by the author.");
            }
        }
    }

    private void updatePossibleNextStates(Ticket ticket) {
        switch (ticket.getState()) {
            case CREATED -> ticket.setPossibleNextStates(Set.of(IN_PROGRESS));
            case IN_PROGRESS -> {
                if (ticket.getEditor().getEmail().equals(userService.getCurrentUser().getEmail())) {
                    ticket.setPossibleNextStates(Set.of(FIXED, REJECTED));
                } else {
                    ticket.setPossibleNextStates(Set.of());
                }
            }
            case FIXED, REJECTED -> {
                if (ticket.getAuthor().getEmail().equals(userService.getCurrentUser().getEmail())) {
                    ticket.setPossibleNextStates(Set.of(REOPENED, CLOSED));
                } else {
                    ticket.setPossibleNextStates(Set.of());
                }
            }
            case REOPENED -> ticket.setPossibleNextStates(Set.of(IN_PROGRESS));
            case CLOSED -> ticket.setPossibleNextStates(Set.of());
            default -> throw new IllegalStateException("Unexpected value: " + ticket.getState());
        }
    }
}
