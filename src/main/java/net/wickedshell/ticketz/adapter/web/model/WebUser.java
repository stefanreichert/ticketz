package net.wickedshell.ticketz.adapter.web.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WebUser {

    @NotNull
    @Size(min = 1, max = 255)
    private String lastname;

    @NotNull
    @Size(min = 1, max = 255)
    private String firstname;

    @Email
    @NotNull
    @Size(min = 1, max = 255)
    private String email;

}
