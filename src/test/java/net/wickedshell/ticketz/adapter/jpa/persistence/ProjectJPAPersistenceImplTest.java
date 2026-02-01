package net.wickedshell.ticketz.adapter.jpa.persistence;

import jakarta.inject.Inject;
import net.wickedshell.ticketz.adapter.jpa.repository.ProjectRepository;
import net.wickedshell.ticketz.service.model.Project;
import net.wickedshell.ticketz.service.port.persistence.exception.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProjectJPAPersistenceImplTest {

    @Inject
    private ProjectRepository projectRepository;

    private ProjectJPAPersistenceImpl unitUnderTest;

    @BeforeEach
    public void setupTest() {
        unitUnderTest = new ProjectJPAPersistenceImpl(projectRepository);
    }

    @Test
    void testCreate_success() {
        // given
        Project project = new Project();
        project.setCode("NEWPROJ");
        project.setName("New Project");
        project.setDescription("Test description");
        project.setActive(true);

        // when
        Project createdProject = unitUnderTest.create(project);

        // then
        assertNotNull(createdProject);
        assertEquals("NEWPROJ", createdProject.getCode());
        assertEquals("New Project", createdProject.getName());
        assertEquals("Test description", createdProject.getDescription());
        assertTrue(createdProject.isActive());
        assertEquals(0L, createdProject.getVersion());
    }

    @Test
    void testUpdate_success() {
        // given
        Project project = unitUnderTest.loadByCode("DEFAULT");
        project.setName("Updated Name");
        project.setDescription("Updated description");

        // when
        Project updatedProject = unitUnderTest.update(project);

        // then
        assertNotNull(updatedProject);
        assertEquals("DEFAULT", updatedProject.getCode());
        assertEquals("Updated Name", updatedProject.getName());
        assertEquals("Updated description", updatedProject.getDescription());
    }

    @Test
    void testLoadByCode_found() {
        // when
        Project project = unitUnderTest.loadByCode("DEFAULT");

        // then
        assertNotNull(project);
        assertEquals("DEFAULT", project.getCode());
        assertEquals("Default Project", project.getName());
        assertTrue(project.isActive());
    }

    @Test
    void testLoadByCode_notFound_throwsException() {
        // when / then
        assertThrows(ObjectNotFoundException.class, () -> unitUnderTest.loadByCode("NONEXISTENT"));
    }

    @Test
    void testFindAll_returnsAllProjects() {
        // when
        List<Project> projects = unitUnderTest.findAll();

        // then
        assertNotNull(projects);
        assertFalse(projects.isEmpty());
        assertTrue(projects.stream().anyMatch(p -> "DEFAULT".equals(p.getCode())));
    }
}
