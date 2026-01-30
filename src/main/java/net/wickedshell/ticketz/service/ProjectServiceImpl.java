package net.wickedshell.ticketz.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.service.exception.ValidationException;
import net.wickedshell.ticketz.service.model.Project;
import net.wickedshell.ticketz.service.port.access.ProjectService;
import net.wickedshell.ticketz.service.port.persistence.ProjectPersistence;
import net.wickedshell.ticketz.service.port.persistence.exception.ObjectNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of ProjectService.
 * Contains business logic and validation for project management.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    
    private final ProjectPersistence projectPersistence;
    
    @Override
    public Project create(@Valid Project project) {
        // New projects are active by default
        project.setActive(true);
        
        // Timestamps managed by JPA @CreationTimestamp and @UpdateTimestamp
        // Code uniqueness enforced by @NaturalId database constraint
        return projectPersistence.create(project);
    }
    
    @Override
    public Project update(@Valid Project project) {
        if (project.getCode() == null || project.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Project code is required for update");
        }
        
        // Verify project exists
        loadByCode(project.getCode());
        
        // Code cannot be changed (it's the business identifier)
        // If you need to change the code, delete the old project and create a new one
        
        // Timestamps managed by JPA @UpdateTimestamp
        return projectPersistence.update(project);
    }
    
    @Override
    public Project loadByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Project code cannot be null or empty");
        }
        return projectPersistence.loadByCode(code);
    }
    
    @Override
    public List<Project> listAll() {
        return projectPersistence.findAll();
    }



    @Override
    public void validateProjectCode(String projectCode) {
        if (projectCode == null || projectCode.trim().isEmpty()) {
            throw new ValidationException("Project code is required");
        }
        try {
            if (!loadByCode(projectCode).isActive()) {
                throw new ValidationException(
                        String.format("Project '%s' is inactive and cannot be used for ticket operations.", projectCode));
            }
        } catch (ObjectNotFoundException exception) {
            throw new ValidationException(
                    String.format("Project with code '%s' not found.", projectCode));
        
        }
    }
}
