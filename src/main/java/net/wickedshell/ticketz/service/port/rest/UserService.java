package net.wickedshell.ticketz.service.port.rest;

import net.wickedshell.ticketz.service.model.User;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);

    User create(User user, String password);

    @PreAuthorize("hasRole('ROLE_USER')")
    User getPrincipalUser();
}
