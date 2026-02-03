package net.wickedshell.ticketz.adapter.rest.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TicketRest {

    @NotBlank
    @Size(max = 80)
    private String title;

    @NotBlank
    @Size(max = 80)
    private String ticketNumber;

    @Size(max = 255)
    private String description;

    @NotNull
    private UserRest author;

    private UserRest editor;

    @NotNull
    private TicketStateRest state;

    private long version;

}
