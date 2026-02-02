package net.wickedshell.ticketz.adapter.web.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketWeb {
    @NotBlank
    private String ticketNumber;
    @NotBlank
    @Size(max = 80)
    private String title;
    @NotBlank
    @Size(max = 255)
    private String description;
    @NotNull
    private UserWeb author;
    private UserWeb editor;
    @NotBlank
    private String state;
    @NotNull
    @Valid
    private ProjectWeb project;

    private boolean canEdit = false;
    private boolean canGoIntoProgress = false;
    private boolean canGoIntoFixed = false;
    private boolean canGoIntoRejected = false;
    private boolean canGoIntoReopened = false;
    private boolean canGoIntoClosed = false;

}
