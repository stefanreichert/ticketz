package net.wickedshell.ticketz.service.port.rest;

import net.wickedshell.ticketz.service.model.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);

    User create(User user, String password);
}
