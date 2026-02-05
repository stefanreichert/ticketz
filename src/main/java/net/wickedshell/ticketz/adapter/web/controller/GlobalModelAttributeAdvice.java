package net.wickedshell.ticketz.adapter.web.controller;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.web.model.UserWeb;
import net.wickedshell.ticketz.core.port.access.UserService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Qualifier("webModelMapper")
    private final ModelMapper mapper;

    @ModelAttribute(ATTRIBUTE_NAME_CURRENT_USER)
    public UserWeb populateCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return mapper.map(userService.getCurrentUser(), UserWeb.class);
        }
        return null;
    }
}
