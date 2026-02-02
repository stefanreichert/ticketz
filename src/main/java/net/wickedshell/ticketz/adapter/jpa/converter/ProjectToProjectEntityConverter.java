package net.wickedshell.ticketz.adapter.jpa.converter;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.jpa.entity.ProjectEntity;
import net.wickedshell.ticketz.adapter.jpa.repository.ProjectRepository;
import net.wickedshell.ticketz.service.model.Project;
import net.wickedshell.ticketz.service.port.persistence.exception.ObjectNotFoundException;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectToProjectEntityConverter implements Converter<Project, ProjectEntity> {

    private final ProjectRepository projectRepository;

    @Override
    public ProjectEntity convert(MappingContext<Project, ProjectEntity> mappingContext) {
        Project project = mappingContext.getSource();
        if (project != null) {
            return this.projectRepository.findByCode(project.getCode()).orElseThrow(ObjectNotFoundException::new);
        }
        return null;
    }
}
