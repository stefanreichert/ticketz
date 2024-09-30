package net.wickedshell.ticketz.adapter.web.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentWeb {

    @NotBlank
    private String text;

    @NotNull
    private UserWeb author;

    @NotNull
    private LocalDateTime dateCreated;

}