package net.wickedshell.ticketz.adapter.rest.controller;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.rest.model.RestTicket;
import net.wickedshell.ticketz.service.model.Ticket;
import net.wickedshell.ticketz.service.port.rest.TicketService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(RestRessource.RESOURCE_TICKETS)
public class TicketController {

    private final TicketService ticketService;
    private final ModelMapper mapper = new ModelMapper();

    @GetMapping
    @PreAuthorize("hasRole('ROLE_API')")
    public ResponseEntity<List<RestTicket>> allTickets() {
        List<RestTicket> restTickets = ticketService.findAll()
                .stream()
                .map(ticket -> mapper.map(ticket, RestTicket.class))
                .toList();
        return ResponseEntity.ok(restTickets);
    }

    @GetMapping(value = "/{ticket-number}")
    @PreAuthorize("hasRole('ROLE_API')")
    public ResponseEntity<RestTicket> oneTicket(@PathVariable("ticket-number") String ticketNumber) {
        RestTicket restTicket = mapper.map(ticketService.loadByTicketNumber(ticketNumber), RestTicket.class);
        return ResponseEntity.ok(restTicket);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_API')")
    public ResponseEntity<RestTicket> create(@RequestBody RestTicket restTicket) {
        Ticket ticket = ticketService.create(mapper.map(restTicket, Ticket.class));
        return ResponseEntity.created(URI.create(RestRessource.RESOURCE_TICKETS + "/" + ticket.getTicketNumber())).build();
    }

    @PutMapping(value = "/{ticket-number}")
    @PreAuthorize("hasRole('ROLE_API')")
    public ResponseEntity<Void> update(@PathVariable("ticket-number") String ticketNumber,
                                       @RequestBody RestTicket restTicket) {
        if (!ticketNumber.equals(restTicket.getTicketNumber())) {
            return ResponseEntity.badRequest().build();
        }
        ticketService.update(mapper.map(restTicket, Ticket.class));
        return ResponseEntity.noContent().build();
    }
}
