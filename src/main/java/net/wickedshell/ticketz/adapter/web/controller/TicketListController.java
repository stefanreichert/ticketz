package net.wickedshell.ticketz.adapter.web.controller;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.service.model.Ticket;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.rest.TicketService;
import net.wickedshell.ticketz.service.port.rest.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

import static net.wickedshell.ticketz.adapter.web.WebAction.ACTION_SHOW_TICKET_LIST;
import static net.wickedshell.ticketz.adapter.web.WebView.VIEW_TICKET_LIST;

@Controller
@RequiredArgsConstructor
public class TicketListController {

    private static final String ATTRIBUTE_NAME_USER = "user";
    private static final String ATTRIBUTE_NAME_TICKETS = "tickets";

    private final UserService userService;
    private final TicketService ticketService;

    @GetMapping(value = ACTION_SHOW_TICKET_LIST)
    public String showTicketList() {
        return VIEW_TICKET_LIST;
    }

    @ModelAttribute(ATTRIBUTE_NAME_USER)
    public User populateUser() {
        return userService.getCurrentUser();
    }

    @ModelAttribute(ATTRIBUTE_NAME_TICKETS)
    public List<Ticket> populateTickets() {
        return ticketService.findAll();
    }
}
