package net.wickedshell.ticketz.adapter.rest.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank
    @Size(max = 255)
    private String lastname;

    @NotBlank
    @Size(max = 255)
    private String firstname;

    @Email
    @NotBlank
    @Size(max = 255)
    private String email;

    @NotBlank
    private String password;
}
