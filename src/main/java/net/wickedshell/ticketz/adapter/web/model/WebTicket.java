package net.wickedshell.ticketz.adapter.web.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WebTicket {
    @NotNull
    private String ticketNumber;
    @NotNull
    @Size(min = 1, max = 80)
    private String title;
    @NotNull
    @Size(min = 1, max = 255)
    private String description;
    @NotNull
    private WebUser author;
    private WebUser editor;
    @NotNull
    private String state;

    private boolean canEdit = false;
    private boolean canGoIntoProgress = false;
    private boolean canGoIntoFixed = false;
    private boolean canGoIntoRejected = false;
    private boolean canGoIntoReopened = false;
    private boolean canGoIntoClosed = false;

}
