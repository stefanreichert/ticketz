package net.wickedshell.ticketz.adapter.rest.controller;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.rest.model.TicketRest;
import net.wickedshell.ticketz.service.model.Ticket;
import net.wickedshell.ticketz.service.port.access.TicketService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(RestRessource.RESOURCE_TICKETS)
public class RestTicketController {

    private final TicketService ticketService;
    private final ModelMapper mapper = new ModelMapper();

    @GetMapping
    @PreAuthorize("hasRole('ROLE_API')")
    public ResponseEntity<List<TicketRest>> allTickets() {
        List<TicketRest> ticketRests = ticketService.findAll()
                .stream()
                .map(ticket -> mapper.map(ticket, TicketRest.class))
                .toList();
        return ResponseEntity.ok(ticketRests);
    }

    @GetMapping(value = "/{ticket-number}")
    @PreAuthorize("hasRole('ROLE_API')")
    public ResponseEntity<TicketRest> oneTicket(@PathVariable("ticket-number") String ticketNumber) {
        TicketRest ticketRest = mapper.map(ticketService.loadByTicketNumber(ticketNumber), TicketRest.class);
        return ResponseEntity.ok(ticketRest);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_API')")
    public ResponseEntity<TicketRest> create(@RequestBody TicketRest ticket) {
        Ticket newTicket = ticketService.create(mapper.map(ticket, Ticket.class));
        return ResponseEntity.created(URI.create(RestRessource.RESOURCE_TICKETS + "/" + newTicket.getTicketNumber())).build();
    }

    @PutMapping(value = "/{ticket-number}")
    @PreAuthorize("hasRole('ROLE_API')")
    public ResponseEntity<Void> update(@PathVariable("ticket-number") String ticketNumber,
                                       @RequestBody TicketRest ticket) {
        if (!ticketNumber.equals(ticket.getTicketNumber())) {
            return ResponseEntity.badRequest().build();
        }
        ticketService.update(mapper.map(ticket, Ticket.class));
        return ResponseEntity.noContent().build();
    }
}
