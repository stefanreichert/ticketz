package net.wickedshell.ticketz.adapter.web.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Signup {

    @NotNull
    @Size(min=1, max=30)
    private String lastname;
    @NotNull
    @Size(min=1, max=30)
    private String firstname;
    @NotNull
    @Size(min=1, max=30)
    private String email;
    @NotNull
    @Size(min=1, max=30)
    private String password;
    @NotNull
    @Size(min=1, max=30)
    private String confirmPassword;
}
