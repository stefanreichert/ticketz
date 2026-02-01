package net.wickedshell.ticketz.adapter.web.controller;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.web.model.ProjectWeb;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.access.ProjectService;
import net.wickedshell.ticketz.service.port.access.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

import static net.wickedshell.ticketz.adapter.web.Action.ACTION_SHOW_PROJECT_LIST;
import static net.wickedshell.ticketz.adapter.web.View.VIEW_PROJECT_LIST;

@Controller
@RequiredArgsConstructor
public class ProjectListController {

    private static final String ATTRIBUTE_NAME_USER = "user";
    private static final String ATTRIBUTE_NAME_PROJECTS = "projects";

    private final UserService userService;
    private final ProjectService projectService;
    private final ModelMapper mapper = new ModelMapper();

    @GetMapping(value = ACTION_SHOW_PROJECT_LIST)
    public String showProjectList() {
        return VIEW_PROJECT_LIST;
    }

    @ModelAttribute(ATTRIBUTE_NAME_USER)
    public User populateUser() {
        return userService.getCurrentUser();
    }

    @ModelAttribute(ATTRIBUTE_NAME_PROJECTS)
    public List<ProjectWeb> populateProjects() {
        return projectService.listAll().stream()
                .map(project -> mapper.map(project, ProjectWeb.class))
                .toList();
    }
}
