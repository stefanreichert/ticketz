package net.wickedshell.ticketz.adapter.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.rest.security.jwt.JwtAuthenticationProvider;
import net.wickedshell.ticketz.adapter.rest.security.jwt.JwtAuthenticationRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class RestAdapterConfiguration {

    @Bean
    public AuthenticationManager authenticationManager(JwtAuthenticationProvider jwtAuthenticationProvider, DaoAuthenticationProvider daoAuthenticationProvider) {
        return new ProviderManager(List.of(jwtAuthenticationProvider, daoAuthenticationProvider));
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsProvider, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsProvider);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationRequestFilter jwtAuthenticationRequestFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .exceptionHandling(handler -> handler.authenticationEntryPoint(new ExceptionEntryPoint()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorization -> authorization
                        .requestMatchers("/authentication/**").anonymous()
                        .requestMatchers("/api/users").authenticated()
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/api/tickets").authenticated()
                        .requestMatchers("/api/tickets/**").authenticated()
                        .anyRequest().denyAll())
                .addFilterBefore(jwtAuthenticationRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private static class ExceptionEntryPoint implements AuthenticationEntryPoint {

        private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionEntryPoint.class);

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
            LOGGER.error(authException.getMessage(), authException);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: unauthorized");
        }
    }
}

