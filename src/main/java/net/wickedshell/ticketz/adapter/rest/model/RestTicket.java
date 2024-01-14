package net.wickedshell.ticketz.adapter.rest.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RestTicket {

    @NotNull
    private String title;

    @NotNull
    private String ticketNumber;

    private String description;

    @NotNull
    private RestUser author;

    private RestUser editor;

    @NotNull
    private RestTicketState state;

    @NotNull
    private Long version;

}
