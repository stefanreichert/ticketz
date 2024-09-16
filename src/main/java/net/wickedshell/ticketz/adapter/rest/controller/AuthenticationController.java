package net.wickedshell.ticketz.adapter.rest.controller;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.rest.model.RestLoginRequest;
import net.wickedshell.ticketz.adapter.rest.model.RestSignupRequest;
import net.wickedshell.ticketz.adapter.rest.security.jwt.JwtAuthenticationRequestFilter;
import net.wickedshell.ticketz.adapter.rest.security.jwt.JwtService;
import net.wickedshell.ticketz.service.model.Role;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.rest.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping(RestRessource.RESOURCE_AUTHENTICATION)
public class AuthenticationController {

    private final ModelMapper mapper = new ModelMapper();
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping(value = "/logins", produces = MimeTypeUtils.TEXT_PLAIN_VALUE)
    @PostAuthorize("hasRole('ROLE_API')")
    public ResponseEntity<String> login(@RequestBody RestLoginRequest loginRequest) {
        AbstractAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        authenticationManager.authenticate(authenticationToken);
        return ResponseEntity.ok(JwtAuthenticationRequestFilter.BEARER_TOKEN_PREFIX + jwtService.createTokenFromEmail(loginRequest.getEmail()));
    }

    @PostMapping(value = "/signups")
    public ResponseEntity<Void> signup(@RequestBody RestSignupRequest signupRequest) {
        User user = mapper.map(signupRequest, User.class);
        userService.create(user, signupRequest.getPassword(), Set.of(Role.ROLE_USER, Role.ROLE_API));
        return ResponseEntity.ok().build();
    }
}
