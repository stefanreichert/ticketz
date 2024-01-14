package net.wickedshell.ticketz.adapter.rest.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class RestUser {

    @NotNull
    private String lastname;

    @NotNull
    private String firstname;

    @NotNull
    private String email;
}
