package net.wickedshell.ticketz.adapter.rest.security;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserDetailsProvider implements org.springframework.security.core.userdetails.UserDetailsService {

    private static final String ANONYMOUS_USER = "anonymousUser";
    private final UserService userService;

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            AnonymousAuthenticationToken anonymousAuthenticationToken =
                    new AnonymousAuthenticationToken(ANONYMOUS_USER, ANONYMOUS_USER, List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
            anonymousAuthenticationToken.setAuthenticated(false);
            SecurityContextHolder.getContext().setAuthentication(anonymousAuthenticationToken);
            Optional<net.wickedshell.ticketz.service.model.User> maybeUser = userService.findByEmail(username);
            if (maybeUser.isEmpty()) {
                throw new UsernameNotFoundException("unknown user");
            }
            net.wickedshell.ticketz.service.model.User user = maybeUser.get();
            List<SimpleGrantedAuthority> authorities =
                    user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.name())).toList();
            return new User(user.getEmail(), user.getPasswordHash(), authorities);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @RequiredArgsConstructor
    private static class User implements org.springframework.security.core.userdetails.UserDetails {

        private final String username;
        private final String passwordHash;
        private final Collection<? extends GrantedAuthority> authorities;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return passwordHash;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
