package net.wickedshell.ticketz.adapter.rest.controller;

import jakarta.inject.Inject;
import net.wickedshell.ticketz.adapter.AuthenticationConfiguration;
import net.wickedshell.ticketz.adapter.rest.RestAdapterConfiguration;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.access.UserService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestAuthenticationController.class)
@ContextConfiguration(classes = {AuthenticationConfiguration.class, RestAdapterConfiguration.class, TestConfiguration.class})
class RestAuthenticationControllerTest {

    public static final String LOGIN_ROUTE = "/api/authentication/logins";
    public static final String LOGIN_REQUEST = "{\"email\":\"%s\",\"password\":\"%s\"}";

    public static final String SIGNUP_ROUTE = "/api/authentication/signups";
    public static final String SIGNUP_REQUEST = "{\"email\":\"%s\",\"password\":\"%s\",\"firstname\":\"%s\",\"lastname\":\"%s\"}";
    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    @Inject
    private MockMvc mvc;
    @Inject
    private WebApplicationContext context;
    @MockBean
    private UserService userService;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void testLogin_success() throws Exception {
        // given
        String password = "test";
        User user = createTestUser(password);
        String requestBody = String.format(LOGIN_REQUEST, user.getEmail(), password);

        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // when
        ResultActions perform = this.mvc.perform(post(LOGIN_ROUTE)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );

        // then
        perform.andExpect(status().isOk()).andExpect(content().string(Matchers.startsWith("Bearer ")));
    }

    @Test
    void testLogin_fail_invalidPassword() throws Exception {
        // given
        String password = "test";
        User user = createTestUser(password);
        String requestBody = String.format(LOGIN_REQUEST, user.getEmail(), "wrong");

        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // when
        ResultActions perform = this.mvc.perform(post(LOGIN_ROUTE)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );

        // then
        perform.andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_fail_unknownUser() throws Exception {
        // given
        String email = "test@us.er";
        String password = "test";
        String requestBody = String.format(LOGIN_REQUEST, email, password);

        when(userService.findByEmail(email)).thenReturn(Optional.empty());

        // when
        ResultActions perform = this.mvc.perform(post(LOGIN_ROUTE)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );

        // then
        perform.andExpect(status().isUnauthorized());
    }

    @Test
    void testSignup_success() throws Exception {
        // given
        String password = "test";
        User user = createTestUser(password);
        String requestBody = String.format(SIGNUP_REQUEST, user.getEmail(), password, user.getFirstname(), user.getLastname());

        when(userService.create(any(User.class), anyString(), anySet())).thenReturn(user);

        // when
        ResultActions perform = this.mvc.perform(post(SIGNUP_ROUTE)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );

        // then
        perform.andExpect(status().isOk());
    }

    @Test
    void testSignup_fail_emailExists() throws Exception {
        // given
        String password = "test";
        User user = createTestUser(password);
        String requestBody = String.format(SIGNUP_REQUEST, user.getEmail(), password, user.getFirstname(), user.getLastname());

        when(userService.create(any(User.class), anyString(), anySet())).thenThrow(DataIntegrityViolationException.class);

        // when
        ResultActions perform = this.mvc.perform(post(SIGNUP_ROUTE)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );

        // then
        perform.andExpect(status().isConflict());
    }

    private User createTestUser(String password) {
        User user = new User();
        user.setEmail("test@us.er");
        user.setFirstname("Test");
        user.setLastname("User");
        user.setPasswordHash(PASSWORD_ENCODER.encode(password));
        return user;
    }

    @Configuration
    public static class TestConfiguration {

        @Bean
        public PasswordEncoder passwordEncoder() {
            return PASSWORD_ENCODER;
        }
    }
}