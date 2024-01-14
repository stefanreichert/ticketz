package net.wickedshell.ticketz.adapter.rest.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestLoginRequest {

    @NotNull
    private String email;
    @NotNull
    private String password;
}
