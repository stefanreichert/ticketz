package net.wickedshell.ticketz.adapter.web.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Signup {

    @NotBlank
    @Size(max = 30)
    private String lastname;
    @NotBlank
    @Size(max = 30)
    private String firstname;
    @NotBlank
    @Size(max = 30)
    private String email;
    @NotBlank
    @Size(max = 30)
    private String password;
    @NotBlank
    @Size(max = 30)
    private String confirmPassword;
}
