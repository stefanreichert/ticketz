package net.wickedshell.ticketz.service.port.driven.persistence;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.wickedshell.ticketz.service.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Driven port for user persistence operations.
 */
public interface UserPersistence {

    /**
     * Load a user by email address.
     *
     * @param email the email address
     * @return the user
     * @throws net.wickedshell.ticketz.service.port.driven.persistence.exception.ObjectNotFoundException if not found
     */
    User loadByEmail(@Email @NotNull @Size(max = 255) String email);

    /**
     * Persist a new user.
     *
     * @param user the user to create
     * @return the created user
     */
    User create(@Valid User user);

    /**
     * Update an existing user.
     *
     * @param user the user with updated data
     * @return the updated user
     * @throws net.wickedshell.ticketz.service.port.driven.persistence.exception.ObjectNotFoundException if not found
     */
    User update(@Valid User user);

    /**
     * Find a user by email address.
     *
     * @param email the email address
     * @return the user if found, empty otherwise
     */
    Optional<User> findByEmail(@Email @NotNull @Size(max = 255) String email);

    /**
     * Find all users.
     *
     * @return list of all users
     */
    List<User> findAll();
}
