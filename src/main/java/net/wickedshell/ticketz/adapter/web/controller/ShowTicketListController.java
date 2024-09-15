package net.wickedshell.ticketz.adapter.web.controller;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.web.WebAction;
import net.wickedshell.ticketz.adapter.web.WebView;
import net.wickedshell.ticketz.service.model.Ticket;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.rest.TicketService;
import net.wickedshell.ticketz.service.port.rest.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ShowTicketListController {

    private final UserService userService;
    private final TicketService ticketService;

    @RequestMapping(value = WebAction.ACTION_SHOW_TICKET_LIST)
    public String showTicketList() {
        return WebView.VIEW_TICKET_LIST;
    }

    @ModelAttribute("user")
    public User populateUser() {
        return userService.getPrincipalUser();
    }

    @ModelAttribute("tickets")
    public List<Ticket> populateTickets() {
        return ticketService.findAll();
    }
}
