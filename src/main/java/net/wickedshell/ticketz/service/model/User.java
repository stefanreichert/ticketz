package net.wickedshell.ticketz.service.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@RequiredArgsConstructor
public class User {

    @NotNull
    @Size(max = 255)
    private String lastname;

    @NotNull
    @Size(max = 255)
    private String firstname;

    @NotNull
    private String passwordHash;

    @Email
    @NotNull
    @Size(max = 255)
    private String email;

    @NotNull
    private Long version;

    @NotNull
    private Set<Role> roles = new HashSet<>();

}
