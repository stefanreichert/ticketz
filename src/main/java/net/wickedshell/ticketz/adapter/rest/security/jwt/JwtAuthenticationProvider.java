package net.wickedshell.ticketz.adapter.rest.security.jwt;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.rest.security.UserDetailsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationProvider.class);
    private final JwtService jwtService;
    private final UserDetailsProvider userDetailsProvider;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String jwt = ((JwtAuthenticationToken) authentication).getJwt();
        if (jwtService.validateToken(jwt)) {
            String email = jwtService.extractEmailFromToken(jwt);
            try {
                UserDetails principal = userDetailsProvider.loadUserByUsername(email);
                return new JwtAuthenticationToken(
                        jwt, principal.getUsername(), principal.getPassword(), principal.getAuthorities());
            } catch (UsernameNotFoundException exception) {
                // fall through, BadCredentials will be thrown below anyway
            }
        }
        throw new BadCredentialsException("Error: Invalid JWT");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(JwtAuthenticationToken.class);
    }
}
