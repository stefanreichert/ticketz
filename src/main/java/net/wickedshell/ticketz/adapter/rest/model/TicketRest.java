package net.wickedshell.ticketz.adapter.rest.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TicketRest {

    @NotNull
    private String title;

    @NotNull
    private String ticketNumber;

    private String description;

    @NotNull
    private UserRest author;

    private UserRest editor;

    @NotNull
    private TicketStateRest state;

    @NotNull
    private long version;

}
