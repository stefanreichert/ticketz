package net.wickedshell.ticketz.service.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Data
@RequiredArgsConstructor
public class Ticket {

    @NotNull
    private String title;

    @NotNull
    @Size(min = 1, max = 80)
    private String ticketNumber;

    @Size(min = 1, max = 255)
    private String description;

    @NotNull
    private User author;

    private User editor;

    @NotNull
    private TicketState state;

    @NotNull
    private Set<TicketState> possibleNextStates;

    @NotNull
    private long version;

}
