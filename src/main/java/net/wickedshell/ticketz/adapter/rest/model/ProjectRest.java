package net.wickedshell.ticketz.adapter.rest.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * REST DTO for Project.
 * Used for API request/response serialization.
 */
@Data
public class ProjectRest {

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Za-z0-9_-]+$")
    private String code;

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String description;

    private boolean active;

    private Long version;
}
