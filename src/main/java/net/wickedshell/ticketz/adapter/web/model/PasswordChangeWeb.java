package net.wickedshell.ticketz.adapter.web.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordChangeWeb {

    @NotBlank
    private String currentPassword;

    @NotBlank
    private String newPassword;

    @NotBlank
    private String confirmPassword;
}
