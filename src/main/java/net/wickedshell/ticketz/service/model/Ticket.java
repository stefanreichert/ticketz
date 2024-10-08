package net.wickedshell.ticketz.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Data
@RequiredArgsConstructor
public class Ticket {

    @NotBlank
    private String title;

    @NotBlank
    @Size(max = 80)
    private String ticketNumber;

    @NotBlank
    @Size(max = 255)
    private String description;

    @NotNull
    private User author;

    private User editor;

    @NotNull
    private TicketState state;

    @NotNull
    private Set<TicketState> possibleNextStates = Set.of();

    @NotNull
    private long version;

}
