package net.wickedshell.ticketz.core.port.access;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import net.wickedshell.ticketz.core.exception.ValidationException;
import net.wickedshell.ticketz.core.model.Project;

import java.util.List;

/**
 * Access port for project management operations.
 */
public interface ProjectService {
    
    /**
     * Create a new project.
     * 
     * @param project the project to create (validated)
     * @return the created project
     * @throws IllegalArgumentException if project data is invalid or code is not unique
     * @throws jakarta.validation.ConstraintViolationException if bean validation fails
     */
    Project create(@Valid Project project);
    
    /**
     * Update an existing project.
     * Updates all fields including name, description, and active status.
     * 
     * @param project the project with updated data (validated, must have code)
     * @return the updated project
     * @throws IllegalArgumentException if project data is invalid or code not found
     * @throws jakarta.validation.ConstraintViolationException if bean validation fails
     */
    Project update(@Valid Project project);
    
    /**
     * Load project by its unique code.
     * 
     * @param code the project code
     * @return Optional containing the project if found, empty otherwise
     */
    Project loadByCode(@NotBlank String code);
    
    /**
     * List all projects.
     * 
     * @return list of all projects (may be empty)
     */
    List<Project> listAll();

    /**
     * Validate that the project exists and is active.
     * Inactive projects cannot have new tickets or modifications.
     * 
     * @param projectCode the project code to validate
     * @throws ValidationException if project code is null, not found, or inactive
     */
    void validateProjectCode(@NotBlank String projectCode);
    
}
