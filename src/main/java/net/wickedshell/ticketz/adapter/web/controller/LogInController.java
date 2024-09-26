package net.wickedshell.ticketz.adapter.web.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.web.model.Login;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

import static net.wickedshell.ticketz.adapter.web.WebAction.*;
import static net.wickedshell.ticketz.adapter.web.WebView.VIEW_LOGIN;

@Controller
@RequiredArgsConstructor
public class LogInController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogInController.class);
    private static final String ATTRIBUTE_NAME_LOGIN = "login";
    private static final String ATTRIBUTE_NAME_MESSAGE = "message";

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final AuthenticationSuccessHandler successHandler;
    private final MessageSource messageSource;

    @GetMapping(ACTION_SHOW_INDEX)
    public ModelAndView showIndex() {
        return new ModelAndView(redirectTo(ACTION_SHOW_LOGIN));
    }

    @GetMapping(ACTION_SHOW_LOGIN)
    public String showLogin(Model model) {
        model.addAttribute(ATTRIBUTE_NAME_LOGIN, new Login());
        return VIEW_LOGIN;
    }

    @PostMapping(ACTION_LOGIN)
    public ModelAndView login(@Valid @ModelAttribute Login login,
                              BindingResult bindingResult,
                              HttpServletRequest request,
                              HttpServletResponse response,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView(VIEW_LOGIN);
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()));
            if (authentication.isAuthenticated()) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
                securityContextRepository.saveContext(context, request, response);
                String message = messageSource.getMessage("message.login_succeeded", null, request.getLocale());
                redirectAttributes.addFlashAttribute(ATTRIBUTE_NAME_MESSAGE, message);
                successHandler.onAuthenticationSuccess(request, response, authentication);
                return null; // Response is already committed by successHandler
            }
        } catch (AuthenticationException | ServletException | IOException exception) {
            LOGGER.info(exception.getMessage());
        }
        String message = messageSource.getMessage("message.login_failed", null, request.getLocale());
        return new ModelAndView(VIEW_LOGIN, "error", message);
    }
}
