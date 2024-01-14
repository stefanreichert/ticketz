package net.wickedshell.ticketz.adapter.rest.controller;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.rest.model.RestLoginRequest;
import net.wickedshell.ticketz.adapter.rest.model.RestSignupRequest;
import net.wickedshell.ticketz.adapter.rest.security.jwt.JwtAuthenticationRequestFilter;
import net.wickedshell.ticketz.adapter.rest.security.jwt.JwtService;
import net.wickedshell.ticketz.service.UserService;
import net.wickedshell.ticketz.service.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/authentication")
public class AuthenticationController {

    private final ModelMapper mapper = new ModelMapper();
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping()
    @RequestMapping("/logins")
    public ResponseEntity<String> login(@RequestBody RestLoginRequest loginRequest) {
        AbstractAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authenticationManager.authenticate(authenticationToken));
        return ResponseEntity.ok(JwtAuthenticationRequestFilter.BEARER_TOKEN_PREFIX + jwtService.createTokenFromEmail(loginRequest.getEmail()));
    }

    @PostMapping()
    @RequestMapping("/signups")
    public ResponseEntity<Void> signup(@RequestBody RestSignupRequest signupRequest) {
        User user = mapper.map(signupRequest, User.class);
        userService.create(user, signupRequest.getPassword());
        return ResponseEntity.ok().build();
    }
}
