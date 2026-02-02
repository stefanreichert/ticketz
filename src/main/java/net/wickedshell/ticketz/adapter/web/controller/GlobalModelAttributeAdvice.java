package net.wickedshell.ticketz.adapter.web.controller;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.access.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {

    private static final String ATTRIBUTE_NAME_CURRENT_USER = "currentUser";

    private final UserService userService;

    @ModelAttribute(ATTRIBUTE_NAME_CURRENT_USER)
    public User populateCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return userService.getCurrentUser();
        }
        return null;
    }
}
