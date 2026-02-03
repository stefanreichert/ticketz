package net.wickedshell.ticketz.service.port.access;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.wickedshell.ticketz.service.model.Role;
import net.wickedshell.ticketz.service.model.User;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserService {
    Optional<User> findByEmail(String email);

    User create(User user, String password, Set<Role> roles);

    @PreAuthorize("hasRole('ROLE_USER')")
    User getCurrentUser();

    @PreAuthorize("hasRole('ROLE_USER')")
    List<User> findAll();

    @PreAuthorize("hasRole('ROLE_USER')")
    User updateName(@Valid User user);

    @PreAuthorize("hasRole('ROLE_USER')")
    User updatePassword(@Email @NotNull @Size(max = 255) String email,
                        @NotBlank String currentPassword,
                        @NotBlank @Size(min = 8) String newPassword);
}
