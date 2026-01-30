package net.wickedshell.ticketz.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.rest.security.exception.AuthenticationException;
import net.wickedshell.ticketz.service.model.Role;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.persistence.UserPersistence;
import net.wickedshell.ticketz.service.port.access.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserPersistence userPersistence;
    private final PasswordEncoder passwordEncoder;

    @Override
    @PreAuthorize("hasRole('ROLE_USER') or isAnonymous()")
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userPersistence.findByEmail(email);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_USER') or isAnonymous()")
    public User create(User user, String password, Set<Role> roles) {
        user.setPasswordHash(passwordEncoder.encode(password));
        user.getRoles().clear();
        user.getRoles().addAll(roles);
        return userPersistence.create(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AuthenticationException authenticationException =
                new AuthenticationException("Error: Invalid Authentication Details");
        if (principal instanceof String principalEmail) {
            return findByEmail(principalEmail).orElseThrow(() -> authenticationException);
        }
        if (principal instanceof org.springframework.security.core.userdetails.User principalUser) {
            return findByEmail(principalUser.getUsername()).orElseThrow(() -> authenticationException);
        }
        throw authenticationException;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userPersistence.findAll();
    }
}
