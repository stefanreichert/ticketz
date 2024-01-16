package net.wickedshell.ticketz.adapter.rest.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationRequestFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_TOKEN_PREFIX = "Bearer ";

    private final AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        parseJwtFromRequest(request).ifPresent(this::authenticate);
        filterChain.doFilter(request, response);
        SecurityContextHolder.clearContext();
    }

    private void authenticate(String jwt) {
        Authentication authentication = authenticationManager.authenticate(new JwtAuthenticationToken(jwt));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    private Optional<String> parseJwtFromRequest(HttpServletRequest request) {
        String authenticationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authenticationHeader != null && authenticationHeader.startsWith(BEARER_TOKEN_PREFIX)) {
            return Optional.of(authenticationHeader.substring(BEARER_TOKEN_PREFIX.length()));
        }
        return Optional.empty();
    }
}