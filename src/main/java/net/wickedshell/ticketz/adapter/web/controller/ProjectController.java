package net.wickedshell.ticketz.adapter.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.web.model.ProjectWeb;
import net.wickedshell.ticketz.service.model.Project;
import net.wickedshell.ticketz.service.port.access.ProjectService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static net.wickedshell.ticketz.adapter.web.Action.*;
import static net.wickedshell.ticketz.adapter.web.View.VIEW_PROJECT;

@Controller
@RequiredArgsConstructor
public class ProjectController {

    private static final String ATTRIBUTE_NAME_PROJECT = "projectWeb";
    private static final String ATTRIBUTE_NAME_MESSAGE = "message";

    @Qualifier("webModelMapper")
    private final ModelMapper mapper;
    private final ProjectService projectService;
    private final MessageSource messageSource;

    @GetMapping(ACTION_NEW_PROJECT)
    public String newProject(Model model) {
        ProjectWeb project = new ProjectWeb();
        project.setNewProject(true);
        project.setActive(true);
        model.addAttribute(ATTRIBUTE_NAME_PROJECT, project);
        return VIEW_PROJECT;
    }

    @GetMapping(ACTION_SHOW_PROJECT)
    public String showProject(@PathVariable String code, Model model) {
        Project existingProject = projectService.loadByCode(code);
        ProjectWeb project = mapper.map(existingProject, ProjectWeb.class);
        project.setNewProject(false);
        model.addAttribute(ATTRIBUTE_NAME_PROJECT, project);
        return VIEW_PROJECT;
    }

    @PostMapping(ACTION_SAVE_PROJECT)
    public ModelAndView saveProject(@PathVariable String code,
                                    @Valid @ModelAttribute ProjectWeb projectWeb,
                                    BindingResult bindingResult,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView(VIEW_PROJECT).addObject(ATTRIBUTE_NAME_PROJECT, projectWeb);
        }

        String messageId;
        String[] arguments;

        if (projectWeb.isNewProject()) {
            Project newProject = projectService.create(mapper.map(projectWeb, Project.class));
            messageId = "message.project.create_succeeded";
            arguments = new String[]{newProject.getCode()};
        } else {
            Project existingProject = projectService.loadByCode(code);
            existingProject.setName(projectWeb.getName());
            existingProject.setDescription(projectWeb.getDescription());
            existingProject.setActive(projectWeb.isActive());
            projectService.update(existingProject);
            messageId = "message.project.save_succeeded";
            arguments = new String[]{existingProject.getCode()};
        }

        redirectAttributes.addFlashAttribute(ATTRIBUTE_NAME_MESSAGE,
                messageSource.getMessage(messageId, arguments, request.getLocale()));
        return new ModelAndView(redirectTo(ACTION_SHOW_PROJECT_LIST));
    }
}
