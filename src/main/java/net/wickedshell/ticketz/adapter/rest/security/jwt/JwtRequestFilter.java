package net.wickedshell.ticketz.adapter.rest.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_TOKEN_PREFIX = "Bearer ";

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        parseJwtFromRequest(request).ifPresent(this::authenticate);
        filterChain.doFilter(request, response);
    }

    private void authenticate(String jwt) {
        AbstractAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(authenticationManager.authenticate(authenticationToken));
    }


    private Optional<String> parseJwtFromRequest(HttpServletRequest request) {
        String authenticationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authenticationHeader != null && authenticationHeader.startsWith(BEARER_TOKEN_PREFIX)) {
            return Optional.of(authenticationHeader.substring(BEARER_TOKEN_PREFIX.length()));
        }
        return Optional.empty();
    }
}