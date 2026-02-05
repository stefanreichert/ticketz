package net.wickedshell.ticketz.core.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {

    @NotBlank
    private String text;

    @NotNull
    private User author;

    private LocalDateTime dateCreated;

}
