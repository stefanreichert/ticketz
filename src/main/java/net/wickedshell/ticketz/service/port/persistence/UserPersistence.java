package net.wickedshell.ticketz.service.port.persistence;

import jakarta.validation.Valid;
import net.wickedshell.ticketz.service.model.User;

import java.util.Optional;

public interface UserPersistence {
    @Valid User loadByEmail(String email);

    @Valid User create(User user);

    @Valid User update(@Valid User user);

    Optional<User> findByEmail(String email);
}
