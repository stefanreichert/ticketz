package net.wickedshell.ticketz.adapter.rest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.rest.model.LoginRequest;
import net.wickedshell.ticketz.adapter.rest.model.SignupRequest;
import net.wickedshell.ticketz.adapter.rest.security.jwt.JwtAuthenticationRequestFilter;
import net.wickedshell.ticketz.adapter.rest.security.jwt.JwtService;
import net.wickedshell.ticketz.service.model.Role;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.access.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping(RestRessource.RESOURCE_AUTHENTICATION)
public class RestAuthenticationController {

    @Qualifier("restModelMapper")
    private final ModelMapper mapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping(value = "/logins", produces = MimeTypeUtils.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest loginRequest) {
        AbstractAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        authenticationManager.authenticate(authenticationToken);
        return ResponseEntity.ok(JwtAuthenticationRequestFilter.BEARER_TOKEN_PREFIX + jwtService.createTokenFromEmail(loginRequest.getEmail()));
    }

    @PostMapping(value = "/signups")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest signupRequest) {
        User user = mapper.map(signupRequest, User.class);
        userService.create(user, signupRequest.getPassword(), Set.of(Role.ROLE_USER, Role.ROLE_API));
        return ResponseEntity.accepted().build();
    }
}
