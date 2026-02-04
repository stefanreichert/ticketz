package net.wickedshell.ticketz.adapter.jpa.persistence;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.jpa.entity.ProjectEntity;
import net.wickedshell.ticketz.adapter.jpa.repository.ProjectRepository;
import net.wickedshell.ticketz.service.model.Project;
import net.wickedshell.ticketz.service.port.driven.persistence.ProjectPersistence;
import net.wickedshell.ticketz.service.port.driven.persistence.exception.ObjectNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Validated
@RequiredArgsConstructor
public class ProjectJPAPersistenceImpl implements ProjectPersistence {

    private static final String PROJECT_NOT_FOUND = "Project not found: %s";

    private final ProjectRepository projectRepository;
    @Qualifier("jpaModelMapper")
    private final ModelMapper mapper;
    
    @Override
    public Project create(Project project) {
        ProjectEntity entity = new ProjectEntity();
        mapper.map(project, entity);
        ProjectEntity savedEntity = projectRepository.save(entity);
        return mapper.map(savedEntity, Project.class);
    }

    @Override
    public Project update(Project project) {
        ProjectEntity existingEntity = projectRepository.findByCode(project.getCode())
                .orElseThrow(() -> new ObjectNotFoundException(String.format(PROJECT_NOT_FOUND, project.getCode())));
        mapper.map(project, existingEntity);
        ProjectEntity savedEntity = projectRepository.save(existingEntity);
        return mapper.map(savedEntity, Project.class);
    }
    
    @Override
    public Project loadByCode(String code) {
        ProjectEntity entity = projectRepository.findByCode(code)
                .orElseThrow(() -> new ObjectNotFoundException(String.format(PROJECT_NOT_FOUND, code)));
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
