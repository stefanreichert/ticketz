package net.wickedshell.ticketz.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Project domain model.
 * Projects organize tickets and provide grouping for related work.
 * Each project has a unique code and can be active or inactive.
 * Inactive projects cannot have new tickets or modifications to existing tickets.
 */
@Data
public class Project {
    
    /**
     * Unique project code (e.g., "PROJ-001", "WEBAPP").
     * Used as the business identifier for the project.
     */
    @NotBlank(message = "Project code is required")
    @Size(max = 50, message = "Project code must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Project code must contain only letters, numbers, hyphens, and underscores")
    private String code;
    
    /**
     * Display name of the project (e.g., "Web Application Rewrite").
     */
    @NotBlank(message = "Project name is required")
    @Size(max = 255, message = "Project name must not exceed 255 characters")
    private String name;
    
    /**
     * Optional project description.
     */
    @Size(max = 1000, message = "Project description must not exceed 1000 characters")
    private String description;
    
    /**
     * Active/inactive status.
     * Inactive projects cannot be modified (no new tickets, no editing/deletion of tickets).
     */
    private boolean active;
    
    /**
     * Timestamp when the project was created.
     */
    private LocalDateTime dateCreated;
    
    /**
     * Timestamp when the project was last updated.
     */
    private LocalDateTime dateUpdated;
    
    /**
     * Version field for optimistic locking.
     */
    private Long version;
}
