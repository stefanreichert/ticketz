package net.wickedshell.ticketz.port.persistence;

import jakarta.validation.Valid;
import net.wickedshell.ticketz.service.model.User;

import java.util.Optional;

public interface UserPersistence {
    @Valid User loadByEmail(String email);

    void delete(String email);

    @Valid User create(User user);

    @Valid User update(@Valid User user);

    Optional<User> findByEmail(String email);
}
