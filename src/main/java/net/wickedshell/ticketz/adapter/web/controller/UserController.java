package net.wickedshell.ticketz.adapter.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.web.model.UserWeb;
import net.wickedshell.ticketz.core.model.Role;
import net.wickedshell.ticketz.core.model.User;
import net.wickedshell.ticketz.core.port.access.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.wickedshell.ticketz.adapter.web.Action.*;
import static net.wickedshell.ticketz.adapter.web.View.VIEW_USER;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserController {

    private static final String ATTRIBUTE_NAME_USER = "user";
    private static final String ATTRIBUTE_NAME_MESSAGE = "message";
    private static final String ATTRIBUTE_NAME_ERROR = "error";
    private static final String ATTRIBUTE_NAME_ALL_ROLES = "allRoles";

    @Qualifier("webModelMapper")
    private final ModelMapper mapper;
    private final UserService userService;
    private final MessageSource messageSource;

    @GetMapping(ACTION_SHOW_USER)
    public String showUser(@PathVariable String email, Model model) {
        User user = userService.findByEmail(email).orElseThrow();
        UserWeb userWeb = mapper.map(user, UserWeb.class);
        model.addAttribute(ATTRIBUTE_NAME_USER, userWeb);
        model.addAttribute(ATTRIBUTE_NAME_ALL_ROLES, List.of(Role.ROLE_USER, Role.ROLE_ADMIN, Role.ROLE_API));
        return VIEW_USER;
    }

    @PostMapping(ACTION_SAVE_USER_ROLES)
    public String saveRoles(@PathVariable String email,
                            @RequestParam(required = false, defaultValue = "") List<String> roles,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        // Validate role strings
        Set<String> validRoleNames = Arrays.stream(Role.values())
                .map(Role::name)
                .collect(Collectors.toSet());
        List<String> invalidRoles = roles.stream()
                .filter(r -> !r.isBlank())
                .filter(r -> !validRoleNames.contains(r))
                .toList();

        if (!invalidRoles.isEmpty()) {
            User user = userService.findByEmail(email).orElseThrow();
            UserWeb userWeb = mapper.map(user, UserWeb.class);
            model.addAttribute(ATTRIBUTE_NAME_USER, userWeb);
            model.addAttribute(ATTRIBUTE_NAME_ALL_ROLES, List.of(Role.ROLE_USER, Role.ROLE_ADMIN, Role.ROLE_API));
            model.addAttribute(ATTRIBUTE_NAME_ERROR, messageSource.getMessage(
                    "message.user.invalid_roles", new String[]{String.join(", ", invalidRoles)}, request.getLocale()));
            return VIEW_USER;
        }

        Set<Role> roleSet = roles.stream()
                .filter(r -> !r.isBlank())
                .map(Role::valueOf)
                .collect(Collectors.toSet());

        userService.updateRoles(email, roleSet);

        String message = messageSource.getMessage("message.user.roles_saved",
                new String[]{email}, request.getLocale());
        redirectAttributes.addFlashAttribute(ATTRIBUTE_NAME_MESSAGE, message);
        return redirectTo(ACTION_SHOW_USER_LIST);
    }
}
