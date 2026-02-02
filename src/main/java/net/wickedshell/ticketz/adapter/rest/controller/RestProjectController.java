package net.wickedshell.ticketz.adapter.rest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.rest.model.ProjectRest;
import net.wickedshell.ticketz.service.model.Project;
import net.wickedshell.ticketz.service.port.access.ProjectService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller for Project management.
 * Provides CRUD operations for projects via REST API.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(RestRessource.RESOURCE_PROJECTS)
public class RestProjectController {

    private final ProjectService projectService;
    @Qualifier("restModelMapper")
    private final ModelMapper mapper;

    /**
     * List all projects.
     *
     * @return list of all projects
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_API')")
    public ResponseEntity<List<ProjectRest>> allProjects() {
        List<ProjectRest> projects = projectService.listAll()
                .stream()
                .map(project -> mapper.map(project, ProjectRest.class))
                .toList();
        return ResponseEntity.ok(projects);
    }

    /**
     * Get a single project by its code.
     *
     * @param code the project code
     * @return the project
     */
    @GetMapping("/{code}")
    @PreAuthorize("hasRole('ROLE_API')")
    public ResponseEntity<ProjectRest> oneProject(@PathVariable("code") String code) {
        Project project = projectService.loadByCode(code);
        return ResponseEntity.ok(mapper.map(project, ProjectRest.class));
    }

    /**
     * Create a new project.
     *
     * @param projectRest the project data
     * @return 201 Created with Location header
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_API')")
    public ResponseEntity<ProjectRest> create(@Valid @RequestBody ProjectRest projectRest) {
        Project newProject = projectService.create(mapper.map(projectRest, Project.class));
        return ResponseEntity
                .created(URI.create(RestRessource.RESOURCE_PROJECTS + "/" + newProject.getCode()))
                .build();
    }

    /**
     * Update an existing project.
     *
     * @param code the project code from URL
     * @param projectRest the updated project data
     * @return 204 No Content on success, 400 if codes don't match
     */
    @PutMapping("/{code}")
    @PreAuthorize("hasRole('ROLE_API')")
    public ResponseEntity<Void> update(@PathVariable("code") String code,
                                       @Valid @RequestBody ProjectRest projectRest) {
        if (!code.equals(projectRest.getCode())) {
            return ResponseEntity.badRequest().build();
        }
        projectService.update(mapper.map(projectRest, Project.class));
        return ResponseEntity.noContent().build();
    }
}
