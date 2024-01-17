package net.wickedshell.ticketz.adapter.rest.controller;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.rest.model.RestTicket;
import net.wickedshell.ticketz.service.model.Ticket;
import net.wickedshell.ticketz.service.port.rest.TicketService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(value = "/{ticket-number}")
    public ResponseEntity<RestTicket> oneTicket(@PathVariable("ticket-number") String ticketNumber) {
        RestTicket restTicket = mapper.map(ticketService.loadByTicketNumber(ticketNumber), RestTicket.class);
        return ResponseEntity.ok(restTicket);
    }

    @PostMapping
    public ResponseEntity<RestTicket> create(@RequestBody RestTicket restTicket) {
        Ticket ticket = ticketService.create(mapper.map(restTicket, Ticket.class));
        return ResponseEntity.ok(mapper.map(ticket, RestTicket.class));
    }

    @PutMapping(value = "/{ticket-number}")
    public ResponseEntity<RestTicket> update(@PathVariable("ticket-number") String ticketNumber,
                                             @RequestBody RestTicket restTicket) {
        if (!ticketNumber.equals(restTicket.getTicketNumber())) {
            return ResponseEntity.badRequest().build();
        }
        Ticket ticket = ticketService.update(mapper.map(restTicket, Ticket.class));
        return ResponseEntity.ok(mapper.map(ticket, RestTicket.class));
    }
}
