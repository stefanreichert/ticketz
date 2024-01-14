package net.wickedshell.ticketz.adapter.rest.security.jwt;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.rest.security.UserDetailsProvider;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtService jwtService;
    private final UserDetailsProvider userDetailsProvider;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String jwt = ((JwtAuthenticationToken) authentication).getJwt();
        if (jwtService.validateToken(jwt)) {
            try {
                String email = jwtService.extractEmailFromToken(jwt);
                UserDetails principal = userDetailsProvider.loadUserByUsername(email);
                return new JwtAuthenticationToken(
                        jwt, principal.getUsername(), principal.getPassword(), principal.getAuthorities());
            } catch (Exception exception) {
                // will result in a BadCredentialsException
            }
        }
        throw new BadCredentialsException("Error: Invalid JWT");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(JwtAuthenticationToken.class);
    }
}
