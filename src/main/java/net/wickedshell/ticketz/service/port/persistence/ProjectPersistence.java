package net.wickedshell.ticketz.service.port.persistence;

import net.wickedshell.ticketz.service.model.Project;

import java.util.List;

/**
 * Port interface for project persistence operations.
 * Follows hexagonal architecture - this is a driven port (implemented by adapters).
 */
public interface ProjectPersistence {
    
    /**
     * Create a new project.
     * 
     * @param project the project to create
     * @return the created project
     */
    Project create(Project project);
    
    /**
     * Update an existing project.
     * The project code is extracted from the project object.
     * 
     * @param project the updated project data
     * @return the updated project
     */
    Project update(Project project);
    
    /**
     * Load project by its unique code.
     * 
     * @param code the project code
     * @return Optional containing the project if found, empty otherwise
     */
    Project loadByCode(String code);
    
    /**
     * Find all projects.
     * 
     * @return list of all projects
     */
    List<Project> findAll();
}
