package net.wickedshell.ticketz.adapter.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.web.Action;
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
import org.springframework.beans.factory.annotation.Qualifier;
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
@RequiredArgsConstructor
public class TicketController {

    private static final String ATTRIBUTE_NAME_TICKET = "ticket";
    private static final String ATTRIBUTE_NAME_MESSAGE = "message";
    private static final String ATTRIBUTE_NAME_COMMENTS = "comments";
    private static final String ATTRIBUTE_NAME_PROJECTS = "projects";

    @Qualifier("webModelMapper")
    private final ModelMapper mapper;
    private final UserService userService;
    private final TicketService ticketService;
    private final CommentService commentService;
    private final ProjectService projectService;
    private final MessageSource messageSource;

    @GetMapping(ACTION_NEW_TICKET)
    public String newTicket(Model model) {
        TicketWeb ticket = new TicketWeb();
        ticket.setTicketNumber(TICKET_NUMBER_NEW);
        ticket.setNewTicket(true);
        ticket.setAuthor(mapper.map(userService.getCurrentUser(), UserWeb.class));
        ticket.setState(CREATED.name());
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
        ticket.setNewTicket(false);
        Project project = existingTicket.getProject();
        ticket.setProjectCode(project.getCode());
        ticket.setProjectName(project.getName());
        ticket.setProjectActive(project.isActive());
        ticket.setCanEdit(ticketService.evaluateCanBeEdited(existingTicket));
        updateWebTicketPossibleTransitions(ticket, existingTicket.getPossibleNextStates());
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

    @PostMapping(ACTION_SAVE_TICKET)
    public ModelAndView createTicket(@PathVariable String ticketNumber, @Valid @ModelAttribute TicketWeb ticket, BindingResult bindingResult, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView(VIEW_TICKET)
                    .addObject(ATTRIBUTE_NAME_TICKET, ticket)
                    .addObject(ATTRIBUTE_NAME_COMMENTS, List.of());
            modelAndView.addObject(ATTRIBUTE_NAME_PROJECTS, getActiveProjects());
            return modelAndView;
        }
        Ticket newTicket = mapper.map(ticket, Ticket.class);
        Project project = projectService.loadByCode(ticket.getProjectCode());
        newTicket.setProject(project);
        Ticket createdTicket = ticketService.create(newTicket);
        redirectAttributes.addFlashAttribute(ATTRIBUTE_NAME_MESSAGE,
                messageSource.getMessage("message.ticket.create_succeeded", new String[]{createdTicket.getTicketNumber()}, request.getLocale()));
        return new ModelAndView(redirectTo(ACTION_SHOW_TICKET_LIST));
    }

    @PostMapping(ACTION_SAVE_TICKET_DETAILS)
    public String saveTicketDetails(@PathVariable String ticketNumber, @Valid @ModelAttribute TicketWeb ticket, BindingResult bindingResult, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ticket.setNewTicket(false);
            List<CommentWeb> comments = commentService.findByTicketNumber(ticketNumber).stream()
                    .map(comment -> mapper.map(comment, CommentWeb.class))
                    .toList();
            model.addAttribute(ATTRIBUTE_NAME_TICKET, ticket);
            model.addAttribute(ATTRIBUTE_NAME_COMMENTS, comments);
            return VIEW_TICKET;
        }
        Ticket existingTicket = ticketService.loadByTicketNumber(ticketNumber);
        existingTicket.setTitle(ticket.getTitle());
        existingTicket.setDescription(ticket.getDescription());
        ticketService.update(existingTicket);
        redirectAttributes.addFlashAttribute(ATTRIBUTE_NAME_MESSAGE,
                messageSource.getMessage("message.ticket.save_succeeded", new String[]{ticketNumber}, request.getLocale()));
        return redirectTo(ACTION_SHOW_TICKET_LIST);
    }

    @PostMapping(ACTION_SAVE_TICKET_STATUS)
    public String changeTicketStatus(@PathVariable String ticketNumber, @RequestParam TicketState newState, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Ticket existingTicket = ticketService.loadByTicketNumber(ticketNumber);
        existingTicket.setState(newState);
        ticketService.update(existingTicket);
        redirectAttributes.addFlashAttribute(ATTRIBUTE_NAME_MESSAGE,
                messageSource.getMessage("message.ticket.status_changed", new String[]{ticketNumber}, request.getLocale()));
        return redirectTo(ACTION_SHOW_TICKET_LIST);
    }

    @PostMapping(ACTION_SAVE_TICKET_COMMENT)
    public String addTicketComment(@PathVariable String ticketNumber, @RequestParam String commentText, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Ticket ticket = ticketService.loadByTicketNumber(ticketNumber);
        Comment comment = new Comment();
        comment.setText(commentText);
        comment.setAuthor(userService.getCurrentUser());
        commentService.create(comment, ticket);
        redirectAttributes.addFlashAttribute(ATTRIBUTE_NAME_MESSAGE,
                messageSource.getMessage("message.ticket.comment_added", new String[]{ticketNumber}, request.getLocale()));
        return redirectTo(ACTION_SHOW_TICKET.replace("{ticketNumber}", ticketNumber));
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
