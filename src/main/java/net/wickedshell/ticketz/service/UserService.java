package net.wickedshell.ticketz.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.port.persistence.UserPersistence;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static net.wickedshell.ticketz.service.model.Role.ROLE_USER;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserPersistence userPersistence;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ROLE_USER') or isAnonymous()")
    public Optional<User> findByEmail(String email) {
        return userPersistence.findByEmail(email);
    }

    @PreAuthorize("hasRole('ROLE_USER') or isAnonymous()")
    public User create(User user, String password) {
        user.setPasswordHash(passwordEncoder.encode(password));
        user.getRoles().clear();
        user.getRoles().add(ROLE_USER);
        return userPersistence.create(user);
    }
}
