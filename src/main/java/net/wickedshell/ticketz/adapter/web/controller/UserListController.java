package net.wickedshell.ticketz.adapter.web.controller;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.web.model.UserWeb;
import net.wickedshell.ticketz.core.port.access.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

import static net.wickedshell.ticketz.adapter.web.Action.ACTION_SHOW_USER_LIST;
import static net.wickedshell.ticketz.adapter.web.View.VIEW_USER_LIST;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserListController {

    private static final String ATTRIBUTE_NAME_USERS = "users";

    private final UserService userService;
    @Qualifier("webModelMapper")
    private final ModelMapper mapper;

    @GetMapping(ACTION_SHOW_USER_LIST)
    public String showUserList(Model model) {
        List<UserWeb> users = userService.findAll().stream()
                .map(user -> mapper.map(user, UserWeb.class))
                .toList();
        model.addAttribute(ATTRIBUTE_NAME_USERS, users);
        return VIEW_USER_LIST;
    }
}
