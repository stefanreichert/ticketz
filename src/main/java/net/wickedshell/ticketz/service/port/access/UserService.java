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

/**
 * Access port for user management operations.
 */
public interface UserService {

    /**
     * Find a user by email address.
     *
     * @param email the email address
     * @return the user if found, empty otherwise
     */
    Optional<User> findByEmail(@Email @NotNull @Size(max = 255) String email);

    /**
     * Create a new user with the given password and roles.
     *
     * @param user     the user to create
     * @param password the plain text password (will be encoded)
     * @param roles    the roles to assign
     * @return the created user
     */
    User create(@Valid User user, @NotBlank @Size(min = 8) String password, @NotNull Set<Role> roles);

    /**
     * Get the currently authenticated user.
     *
     * @return the current user
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    User getCurrentUser();

    /**
     * List all users.
     *
     * @return list of all users
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    List<User> findAll();

    /**
     * Update the firstname and lastname of a user. Only name fields are modified;
     * email, password, and roles remain unchanged.
     *
     * @param user the user with updated name fields
     * @return the updated user
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    User updateName(@Valid User user);

    /**
     * Change a user's password. Requires verification of the current password.
     *
     * @param email           the user's email address
     * @param currentPassword the current password for verification
     * @param newPassword     the new password (minimum 8 characters)
     * @return the updated user
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    User updatePassword(@Email @NotNull @Size(max = 255) String email,
                        @NotBlank String currentPassword,
                        @NotBlank @Size(min = 8) String newPassword);
}
