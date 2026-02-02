package net.wickedshell.ticketz.adapter.web;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.web.converter.WebUserToUserConverter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.*;

@RequiredArgsConstructor
@ComponentScan(basePackageClasses = WebAdapterConfiguration.class)
@Configuration
public class WebAdapterConfiguration {

    @Bean
    public SecurityFilterChain webFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository, AuthenticationSuccessHandler authenticationSuccessHandler) throws Exception {
        return http
                .csrf(Customizer.withDefaults())
                .cors(Customizer.withDefaults())
                .formLogin(form -> form
                        .loginPage(Action.ACTION_SHOW_LOGIN)
                        .successHandler(authenticationSuccessHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl(Action.ACTION_LOGOUT)
                        .logoutSuccessUrl(Action.ACTION_SHOW_LOGIN)
                        .permitAll()
                )
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(securityContextRepository))
                .authorizeHttpRequests(authorization -> authorization
                        .requestMatchers("/secure/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(new SecurityContextHolderFilter(securityContextRepository),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl(Action.ACTION_SHOW_TICKET_LIST);
        return handler;
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository());
    }

    @Bean
    public ModelMapper webModelMapper(WebUserToUserConverter webUserToUserConverter) {
        ModelMapper mapper = new ModelMapper();
        mapper.addConverter(webUserToUserConverter);
        return mapper;
    }
}

