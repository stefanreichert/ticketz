package net.wickedshell.ticketz.adapter.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.web.WebAction;
import net.wickedshell.ticketz.adapter.web.WebView;
import net.wickedshell.ticketz.adapter.web.model.Login;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class LogInController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogInController.class);

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @GetMapping(WebAction.ACTION_SHOW_INDEX)
    public ModelAndView showIndex() {
        return new ModelAndView("redirect:" + WebAction.ACTION_SHOW_LOGIN);
    }

    @GetMapping(WebAction.ACTION_SHOW_LOGIN)
    public String showLogin(Model model) {
        model.addAttribute("login", new Login());
        return WebView.VIEW_LOGIN;
    }

    @PostMapping(WebAction.ACTION_LOGIN)
    public ModelAndView login(@ModelAttribute Login login,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()));
            if (authentication.isAuthenticated()) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
                securityContextRepository.saveContext(context, request, response);
                return new ModelAndView("redirect:" + WebAction.ACTION_SHOW_TICKET_LIST);
            }
        } catch (AuthenticationException exception) {
            LOGGER.info(exception.getMessage());
        }
        return new ModelAndView(WebView.VIEW_LOGIN, "error", "Invalid email or password");
    }
}
