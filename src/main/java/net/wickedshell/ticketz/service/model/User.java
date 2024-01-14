package net.wickedshell.ticketz.service.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@RequiredArgsConstructor
public class User {

    @NotNull
    private String lastname;

    @NotNull
    private String firstname;

    @NotNull
    private String passwordHash;

    @NotNull
    private String email;

    @NotNull
    private Long version;

    @NotNull
    private Set<Role> roles = new HashSet<>();


}
