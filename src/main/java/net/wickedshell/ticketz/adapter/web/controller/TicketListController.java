package net.wickedshell.ticketz.adapter.web.controller;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.web.model.TicketWeb;
import net.wickedshell.ticketz.service.port.access.TicketService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

import static net.wickedshell.ticketz.adapter.web.Action.ACTION_SHOW_TICKET_LIST;
import static net.wickedshell.ticketz.adapter.web.View.VIEW_TICKET_LIST;

@Controller
@RequiredArgsConstructor
public class TicketListController {

    private static final String ATTRIBUTE_NAME_TICKETS = "tickets";

    private final TicketService ticketService;

    @Qualifier("webModelMapper")
    private final ModelMapper mapper;

    @GetMapping(value = ACTION_SHOW_TICKET_LIST)
    public String showTicketList() {
        return VIEW_TICKET_LIST;
    }

    @ModelAttribute(ATTRIBUTE_NAME_TICKETS)
    public List<TicketWeb
    > populateTickets() {
        return ticketService.findAll().stream()
                .map(ticket -> mapper.map(ticket, TicketWeb.class))
                .toList();
    }
}
