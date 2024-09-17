package net.wickedshell.ticketz.adapter.web.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Login {

    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;

}
