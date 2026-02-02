package net.wickedshell.ticketz.service;

import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.service.exception.ValidationException;
import net.wickedshell.ticketz.service.model.Comment;
import net.wickedshell.ticketz.service.model.Project;
import net.wickedshell.ticketz.service.model.Ticket;
import net.wickedshell.ticketz.service.model.TicketState;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.access.CommentService;
import net.wickedshell.ticketz.service.port.access.ProjectService;
import net.wickedshell.ticketz.service.port.access.TicketService;
import net.wickedshell.ticketz.service.port.access.UserService;
import net.wickedshell.ticketz.service.port.persistence.TicketPersistence;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static net.wickedshell.ticketz.service.model.TicketState.*;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private static final String TICKET_NUMBER_TEMPLATE = "%s-%d";
    private final TicketPersistence ticketPersistence;
    private final UserService userService;
    private final CommentService commentService;
    private final ProjectService projectService;

    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional(readOnly = true)
    public Ticket loadByTicketNumber(String ticketNumber) {
        Ticket ticket = ticketPersistence.loadByTicketNumber(ticketNumber);
        updatePossibleNextStates(ticket);
        return ticket;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    public void deleteByTicketNumber(String ticketNumber) {
        Ticket ticket = ticketPersistence.loadByTicketNumber(ticketNumber);
        validateProject(ticket.getProject());
        ticketPersistence.deleteByTicketNumber(ticketNumber);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    public Ticket create(@Valid Ticket ticket) {
        validateProject(ticket.getProject());
        
        long nextTicketNumber = ticketPersistence.getTicketCount() + 1;
        ticket.setTicketNumber(String.format(TICKET_NUMBER_TEMPLATE, ticket.getProject().getCode(), nextTicketNumber));
        ticket.setState(CREATED);
        ticket.setAuthor(userService.getCurrentUser());
        return ticketPersistence.create(ticket);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    public Ticket update(Ticket ticket) {
        Ticket existingTicket = ticketPersistence.loadByTicketNumber(ticket.getTicketNumber());
        
        // Validate project is active before allowing updates
        validateProject(ticket.getProject());
        
        if (!evaluateCanBeEdited(existingTicket)) {
            throw new ValidationException(
                    String.format("Invalid action for state: ticket cannot be edited: %s in state %s.",
                            existingTicket.getTicketNumber(), existingTicket.getState()));
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
    public Ticket updateWithComment(@Valid Ticket ticket, @Valid Comment comment) {
        comment.setAuthor(userService.getCurrentUser());
        commentService.create(comment, ticket);
        return update(ticket);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional(readOnly = true)
    public List<Ticket> findAll() {
        List<Ticket> tickets = ticketPersistence.findAll();
        tickets.forEach(this::updatePossibleNextStates);
        return tickets;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    public boolean evaluateCanBeEdited(@Valid Ticket ticket) {
        if (!ticket.getProject().isActive()) {
            return false;
        }
        if (ticket.getState() == CLOSED) {
            return false;
        }
        if (Set.of(FIXED, REJECTED).contains(ticket.getState())) {
            return userService.getCurrentUser().getEmail().equals(ticket.getAuthor().getEmail());
        }
        if (ticket.getState() == IN_PROGRESS) {
            return userService.getCurrentUser().getEmail().equals(ticket.getEditor().getEmail());
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
        if (ticket == null || ticket.getState() == null) {
            return;
        }
        if (!ticket.getProject().isActive()) {
            ticket.setPossibleNextStates(Set.of());
            return;
        }
        switch (ticket.getState()) {
            case IN_PROGRESS -> {
                if (ticket.getEditor().getEmail().equals(userService.getCurrentUser().getEmail())) {
                    ticket.setPossibleNextStates(ticket.getState().getPermittedSuccessors());
                } else {
                    ticket.setPossibleNextStates(Set.of());
                }
            }
            case FIXED, REJECTED -> {
                if (ticket.getAuthor().getEmail().equals(userService.getCurrentUser().getEmail())) {
                    ticket.setPossibleNextStates(ticket.getState().getPermittedSuccessors());
                } else {
                    ticket.setPossibleNextStates(Set.of());
                }
            }
            case CREATED, REOPENED, CLOSED -> ticket.setPossibleNextStates(ticket.getState().getPermittedSuccessors());
            default -> throw new IllegalStateException("Unexpected value: " + ticket.getState());
        }
    }

    private void validateProject(Project project) {
        if (project == null) {
            throw new ValidationException("Project is required");
        }
        projectService.validateProjectCode(project.getCode());
    }
}
