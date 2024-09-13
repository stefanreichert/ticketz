package net.wickedshell.ticketz.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.rest.security.exception.AuthenticationException;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.persistence.UserPersistence;
import net.wickedshell.ticketz.service.port.rest.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static net.wickedshell.ticketz.service.model.Role.ROLE_USER;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserPersistence userPersistence;
    private final PasswordEncoder passwordEncoder;

    @Override
    @PreAuthorize("hasRole('ROLE_USER') or isAnonymous()")
    public Optional<User> findByEmail(String email) {
        return userPersistence.findByEmail(email);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_USER') or isAnonymous()")
    public User create(User user, String password) {
        user.setPasswordHash(passwordEncoder.encode(password));
        user.getRoles().clear();
        user.getRoles().add(ROLE_USER);
        return userPersistence.create(user);
    }

    @Override
    public User getPrincipalUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AuthenticationException authenticationException =
                new AuthenticationException("Error: Invalid Authentication Details");
        if (principal instanceof String principalEmail) {
            return findByEmail(principalEmail).orElseThrow(() -> authenticationException);
        }
        throw authenticationException;
    }
}
