package net.wickedshell.ticketz.service.port.driven.persistence;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import net.wickedshell.ticketz.service.model.Project;

import java.util.List;

/**
 * Driven port for project persistence operations.
 */
public interface ProjectPersistence {

    /**
     * Persist a new project.
     *
     * @param project the project to create
     * @return the created project
     */
    Project create(@Valid Project project);

    /**
     * Update an existing project.
     *
     * @param project the project with updated data
     * @return the updated project
     * @throws net.wickedshell.ticketz.service.port.driven.persistence.exception.ObjectNotFoundException if not found
     */
    Project update(@Valid Project project);

    /**
     * Load a project by its unique code.
     *
     * @param code the project code
     * @return the project
     * @throws net.wickedshell.ticketz.service.port.driven.persistence.exception.ObjectNotFoundException if not found
     */
    Project loadByCode(@NotBlank String code);

    /**
     * Find all projects.
     *
     * @return list of all projects
     */
    List<Project> findAll();
}
