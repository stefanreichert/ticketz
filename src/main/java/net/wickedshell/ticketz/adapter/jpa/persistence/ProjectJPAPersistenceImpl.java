package net.wickedshell.ticketz.adapter.jpa.persistence;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.jpa.entity.ProjectEntity;
import net.wickedshell.ticketz.adapter.jpa.repository.ProjectRepository;
import net.wickedshell.ticketz.service.model.Project;
import net.wickedshell.ticketz.service.port.persistence.ProjectPersistence;
import net.wickedshell.ticketz.service.port.persistence.exception.ObjectNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JPA implementation of ProjectPersistence port.
 * Handles persistence operations for projects using Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class ProjectJPAPersistenceImpl implements ProjectPersistence {

    private final ProjectRepository projectRepository;
    @Qualifier("jpaModelMapper")
    private final ModelMapper mapper;
    
    @Override
    public Project create(Project project) {
        ProjectEntity entity = mapper.map(project, ProjectEntity.class);
        ProjectEntity savedEntity = projectRepository.save(entity);
        return mapper.map(savedEntity, Project.class);
    }
    
    @Override
    public Project update(Project project) {
        // Extract code from project and find existing entity
        String code = project.getCode();
        ProjectEntity existingEntity = projectRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Project not found with code: " + code));
        
        // Map updated project to entity, preserving the ID
        ProjectEntity updatedEntity = mapper.map(project, ProjectEntity.class);
        updatedEntity.setId(existingEntity.getId());
        updatedEntity.setVersion(existingEntity.getVersion());
        
        ProjectEntity savedEntity = projectRepository.save(updatedEntity);
        return mapper.map(savedEntity, Project.class);
    }
    
    @Override
    public Project loadByCode(String code) {
        ProjectEntity entity = projectRepository.findByCode(code).orElseThrow(ObjectNotFoundException::new);
        return mapper.map(entity, Project.class);
    }
    
    @Override
    public List<Project> findAll() {
        return projectRepository.findAll()
                .stream()
                .map(entity -> mapper.map(entity, Project.class))
                .collect(Collectors.toList());
    }
}
