package net.wickedshell.ticketz.adapter.web.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeWeb {

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8)
    private String newPassword;

    @NotBlank
    @Size(min = 8)
    private String confirmPassword;
}
