package net.wickedshell.ticketz.service.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Ticket {

    @NotNull
    private String title;

    @NotNull
    private String ticketNumber;

    private String description;

    @NotNull
    private User author;

    private User editor;

    @NotNull
    private TicketState state;

    @NotNull
    private Long version;

}
