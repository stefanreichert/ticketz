package net.wickedshell.ticketz.service.port.access;

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
}
