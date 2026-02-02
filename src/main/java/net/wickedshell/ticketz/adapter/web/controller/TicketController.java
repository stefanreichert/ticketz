package net.wickedshell.ticketz.adapter.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import net.wickedshell.ticketz.adapter.web.Action;
import net.wickedshell.ticketz.adapter.web.converter.WebUserToUserConverter;
import net.wickedshell.ticketz.adapter.web.model.CommentWeb;
import net.wickedshell.ticketz.adapter.web.model.TicketWeb;
import net.wickedshell.ticketz.adapter.web.model.UserWeb;
import net.wickedshell.ticketz.adapter.web.model.ProjectWeb;
import net.wickedshell.ticketz.service.model.Comment;
import net.wickedshell.ticketz.service.model.Project;
import net.wickedshell.ticketz.service.model.Ticket;
import net.wickedshell.ticketz.service.model.TicketState;
import net.wickedshell.ticketz.service.port.access.CommentService;
import net.wickedshell.ticketz.service.port.access.ProjectService;
import net.wickedshell.ticketz.service.port.access.TicketService;
import net.wickedshell.ticketz.service.port.access.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

import static net.wickedshell.ticketz.adapter.web.Action.*;
import static net.wickedshell.ticketz.adapter.web.View.VIEW_TICKET;
import static net.wickedshell.ticketz.service.model.TicketState.*;

@Controller
public class TicketController {

    private static final String ATTRIBUTE_NAME_TICKET = "ticketWeb";
    private static final String ATTRIBUTE_NAME_MESSAGE = "message";
    private static final String ATTRIBUTE_NAME_COMMENTS = "comments";
    private static final String ATTRIBUTE_NAME_PROJECTS = "projects";

    private final ModelMapper mapper = new ModelMapper();

    private final UserService userService;
    private final TicketService ticketService;
    private final CommentService commentService;
    private final ProjectService projectService;
    private final MessageSource messageSource;

    public TicketController(UserService userService, TicketService ticketService, CommentService commentService, ProjectService projectService, MessageSource messageSource, WebUserToUserConverter userConverter) {
        this.userService = userService;
        this.ticketService = ticketService;
        this.commentService = commentService;
        this.projectService = projectService;
        this.messageSource = messageSource;
        this.mapper.addConverter(userConverter);
    }

    @GetMapping(ACTION_NEW_TICKET)
    public String newTicket(Model model) {
        TicketWeb ticket = new TicketWeb();
        ticket.setTicketNumber(TICKET_NUMBER_NEW);
        ticket.setAuthor(mapper.map(userService.getCurrentUser(), UserWeb.class));
        ticket.setState(CREATED.name());
        ticket.setProject(new ProjectWeb());
        ticket.setCanEdit(true);
        model.addAttribute(ATTRIBUTE_NAME_TICKET, ticket);
        model.addAttribute(ATTRIBUTE_NAME_COMMENTS, List.of());
        model.addAttribute(ATTRIBUTE_NAME_PROJECTS, getActiveProjects());
        return VIEW_TICKET;
    }

    @GetMapping(ACTION_SHOW_TICKET)
    public String showTicket(@PathVariable String ticketNumber, Model model) {
        Ticket existingTicket = ticketService.loadByTicketNumber(ticketNumber);
        TicketWeb ticket = mapper.map(existingTicket, TicketWeb.class);
        ticket.setProject(mapper.map(existingTicket.getProject(), ProjectWeb.class));
        boolean projectActive = existingTicket.getProject().isActive();
        ticket.setCanEdit(ticketService.evaluateCanBeEdited(existingTicket) && projectActive);
        if (projectActive) {
            updateWebTicketPossibleTransitions(ticket, existingTicket.getPossibleNextStates());
        }
        List<Comment> comments = commentService.findByTicketNumber(ticketNumber);
        model.addAttribute(ATTRIBUTE_NAME_TICKET, ticket);
        model.addAttribute(ATTRIBUTE_NAME_COMMENTS, comments.stream()
                .map(comment -> mapper.map(comment, CommentWeb.class))
                .toList());
        return VIEW_TICKET;
    }

    @GetMapping(Action.ACTION_DELETE_TICKET)
    public String deleteTicket(@PathVariable String ticketNumber, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        ticketService.deleteByTicketNumber(ticketNumber);
        String[] arguments = new String[]{ticketNumber};
        String message = messageSource.getMessage("message.ticket.delete_succeeded", arguments, request.getLocale());
        redirectAttributes.addFlashAttribute(ATTRIBUTE_NAME_MESSAGE, message);
        return redirectTo(ACTION_SHOW_TICKET_LIST);
    }

    @PostMapping(Action.ACTION_SAVE_TICKET)
    public ModelAndView saveTicket(@PathVariable String ticketNumber, @RequestParam TicketState newState, @RequestParam(required = false) String commentText, @Valid @ModelAttribute TicketWeb ticket, BindingResult bindingResult, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            List<CommentWeb> comments = commentService.findByTicketNumber(ticketNumber).stream()
                    .map(comment -> mapper.map(comment, CommentWeb.class))
                    .toList();
            ModelAndView modelAndView = new ModelAndView(VIEW_TICKET)
                    .addObject(ATTRIBUTE_NAME_TICKET, ticket)
                    .addObject(ATTRIBUTE_NAME_COMMENTS, comments);
            if (TICKET_NUMBER_NEW.equals(ticketNumber)) {
                modelAndView.addObject(ATTRIBUTE_NAME_PROJECTS, getActiveProjects());
            }
            return modelAndView;
        }
        String messageId;
        String[] arguments;
        if (TICKET_NUMBER_NEW.equals(ticketNumber)) {
            Ticket newTicket = mapper.map(ticket, Ticket.class);
            Project project = projectService.loadByCode(ticket.getProject().getCode());
            newTicket.setProject(project);
            Ticket createdTicket = ticketService.create(newTicket);
            messageId = "message.ticket.create_succeeded";
            arguments = new String[]{createdTicket.getTicketNumber()};
        } else {
            Ticket existingTicket = ticketService.loadByTicketNumber(ticketNumber);
            existingTicket.setState(newState);
            existingTicket.setTitle(ticket.getTitle());
            existingTicket.setDescription(ticket.getDescription());
            if (!commentText.isBlank()) {
                Comment comment = new Comment();
                comment.setText(commentText);
                ticketService.updateWithComment(existingTicket, comment);
            } else {
                ticketService.update(existingTicket);
            }
            messageId = "message.ticket.save_succeeded";
            arguments = new String[]{existingTicket.getTicketNumber()};
        }
        redirectAttributes.addFlashAttribute(ATTRIBUTE_NAME_MESSAGE,
                messageSource.getMessage(messageId, arguments, request.getLocale()));
        return new ModelAndView(redirectTo(ACTION_SHOW_TICKET_LIST));
    }

    private void updateWebTicketPossibleTransitions(TicketWeb ticketWeb, Set<TicketState> possibleNextStates) {
        ticketWeb.setCanGoIntoProgress(possibleNextStates.contains(IN_PROGRESS));
        ticketWeb.setCanGoIntoFixed(possibleNextStates.contains(FIXED));
        ticketWeb.setCanGoIntoRejected(possibleNextStates.contains(REJECTED));
        ticketWeb.setCanGoIntoClosed(possibleNextStates.contains(CLOSED));
        ticketWeb.setCanGoIntoReopened(possibleNextStates.contains(REOPENED));
    }

    private List<ProjectWeb> getActiveProjects() {
        return projectService.listAll().stream()
                .filter(Project::isActive)
                .map(project -> mapper.map(project, ProjectWeb.class))
                .toList();
    }
}
