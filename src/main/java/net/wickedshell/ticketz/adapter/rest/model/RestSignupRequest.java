package net.wickedshell.ticketz.adapter.rest.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RestSignupRequest {

    @NotNull
    private String lastname;

    @NotNull
    private String firstname;

    @NotNull
    private String email;

    @NotNull
    private String password;
}
