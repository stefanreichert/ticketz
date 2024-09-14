package net.wickedshell.ticketz.adapter.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.*;

import java.io.IOException;

@RequiredArgsConstructor
@ComponentScan(basePackageClasses = WebAdapterConfiguration.class)
@Configuration
public class WebAdapterConfiguration {

    @Bean
    public SecurityFilterChain webFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        return http
                .csrf(Customizer.withDefaults())
                .cors(Customizer.withDefaults())
                .exceptionHandling(handler -> handler.authenticationEntryPoint(new ExceptionEntryPoint()))
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(securityContextRepository))
                .authorizeHttpRequests(authorization -> authorization
                        .requestMatchers("/secure/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(new SecurityContextHolderFilter(securityContextRepository), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository());
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

