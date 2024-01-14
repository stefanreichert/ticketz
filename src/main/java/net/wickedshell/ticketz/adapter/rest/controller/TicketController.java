package net.wickedshell.ticketz.adapter.rest.controller;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.rest.model.RestTicket;
import net.wickedshell.ticketz.service.TicketService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final ModelMapper mapper = new ModelMapper();

    @GetMapping
    public ResponseEntity<List<RestTicket>> allTickets() {
        List<RestTicket> restTickets = ticketService.findAll()
                .stream()
                .map(ticket -> mapper.map(ticket, RestTicket.class))
                .toList();
        return ResponseEntity.ok(restTickets);
    }

    @GetMapping
    @RequestMapping("/{ticket-number}")
    public ResponseEntity<RestTicket> oneTicket(@PathVariable("ticket-number") String ticketNumber) {
        RestTicket restTicket = mapper.map(ticketService.loadByTicketNumber(ticketNumber), RestTicket.class);
        return ResponseEntity.ok(restTicket);
    }
}
