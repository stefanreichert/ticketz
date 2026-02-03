package net.wickedshell.ticketz.service.port.driven.persistence;

import jakarta.validation.Valid;
import net.wickedshell.ticketz.service.model.User;

import java.util.List;
import java.util.Optional;

public interface UserPersistence {
    @Valid User loadByEmail(String email);

    @Valid User create(User user);

    @Valid User update(@Valid User user);

    Optional<User> findByEmail(String email);

    List<User> findAll();
}
