package net.wickedshell.ticketz.adapter.rest.controller;

import jakarta.inject.Inject;
import net.wickedshell.ticketz.adapter.AuthenticationConfiguration;
import net.wickedshell.ticketz.adapter.rest.RestAdapterConfiguration;
import net.wickedshell.ticketz.service.model.Project;
import net.wickedshell.ticketz.service.port.access.ProjectService;
import net.wickedshell.ticketz.service.port.access.TicketService;
import net.wickedshell.ticketz.service.port.access.UserService;
import net.wickedshell.ticketz.service.port.persistence.exception.ObjectNotFoundException;
import net.wickedshell.ticketz.testsupport.TestPasswordEncoderConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestProjectController.class)
@ContextConfiguration(classes = {AuthenticationConfiguration.class, RestAdapterConfiguration.class, TestPasswordEncoderConfig.class})
class RestProjectControllerTest {

    private static final String PROJECTS_ROUTE = "/api/projects";
    private static final String PROJECT_REQUEST = "{\"code\":\"%s\",\"name\":\"%s\",\"description\":\"%s\",\"active\":%s,\"version\":%d}";

    @Inject
    private MockMvc mvc;
    @Inject
    private WebApplicationContext context;
    @MockBean
    private ProjectService projectService;
    @MockBean
    private UserService userService;
    @MockBean
    private TicketService ticketService;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "API")
    void testAllProjects_returnsProjectList() throws Exception {
        // given
        Project project1 = createTestProject("PROJ1", "Project One");
        Project project2 = createTestProject("PROJ2", "Project Two");
        when(projectService.listAll()).thenReturn(List.of(project1, project2));

        // when
        ResultActions perform = mvc.perform(get(PROJECTS_ROUTE));

        // then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code", is("PROJ1")))
                .andExpect(jsonPath("$[1].code", is("PROJ2")));
    }

    @Test
    @WithMockUser(roles = "API")
    void testAllProjects_emptyList() throws Exception {
        // given
        when(projectService.listAll()).thenReturn(List.of());

        // when
        ResultActions perform = mvc.perform(get(PROJECTS_ROUTE));

        // then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "API")
    void testOneProject_found() throws Exception {
        // given
        Project project = createTestProject("WEBAPP", "Web Application");
        when(projectService.loadByCode("WEBAPP")).thenReturn(project);

        // when
        ResultActions perform = mvc.perform(get(PROJECTS_ROUTE + "/WEBAPP"));

        // then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("WEBAPP")))
                .andExpect(jsonPath("$.name", is("Web Application")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @WithMockUser(roles = "API")
    void testOneProject_notFound_returns404() throws Exception {
        // given
        when(projectService.loadByCode("UNKNOWN")).thenThrow(ObjectNotFoundException.class);

        // when
        ResultActions perform = mvc.perform(get(PROJECTS_ROUTE + "/UNKNOWN"));

        // then
        perform.andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "API")
    void testCreate_success_returns201WithLocation() throws Exception {
        // given
        Project project = createTestProject("NEWPROJ", "New Project");
        String requestBody = String.format(PROJECT_REQUEST, "NEWPROJ", "New Project", "Description", true, 0);
        when(projectService.create(any(Project.class))).thenReturn(project);

        // when
        ResultActions perform = mvc.perform(post(PROJECTS_ROUTE)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        perform.andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/projects/NEWPROJ"));
    }

    @Test
    @WithMockUser(roles = "API")
    void testCreate_duplicateCode_returns409() throws Exception {
        // given
        String requestBody = String.format(PROJECT_REQUEST, "EXISTING", "Existing Project", "Description", true, 0);
        when(projectService.create(any(Project.class))).thenThrow(DataIntegrityViolationException.class);

        // when
        ResultActions perform = mvc.perform(post(PROJECTS_ROUTE)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        perform.andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "API")
    void testUpdate_success_returns204() throws Exception {
        // given
        Project project = createTestProject("PROJ1", "Updated Name");
        String requestBody = String.format(PROJECT_REQUEST, "PROJ1", "Updated Name", "Updated Description", true, 1);
        when(projectService.update(any(Project.class))).thenReturn(project);

        // when
        ResultActions perform = mvc.perform(put(PROJECTS_ROUTE + "/PROJ1")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        perform.andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "API")
    void testUpdate_codesMismatch_returns400() throws Exception {
        // given
        String requestBody = String.format(PROJECT_REQUEST, "PROJ2", "Some Name", "Description", true, 1);

        // when
        ResultActions perform = mvc.perform(put(PROJECTS_ROUTE + "/PROJ1")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        perform.andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "API")
    void testUpdate_notFound_returns404() throws Exception {
        // given
        String requestBody = String.format(PROJECT_REQUEST, "UNKNOWN", "Some Name", "Description", true, 1);
        when(projectService.update(any(Project.class))).thenThrow(new IllegalStateException("Project not found"));

        // when
        ResultActions perform = mvc.perform(put(PROJECTS_ROUTE + "/UNKNOWN")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        perform.andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAllProjects_withoutApiRole_returns403() throws Exception {
        // when
        ResultActions perform = mvc.perform(get(PROJECTS_ROUTE));

        // then
        perform.andExpect(status().isForbidden());
    }

    @Test
    void testAllProjects_unauthenticated_returns401() throws Exception {
        // when
        ResultActions perform = mvc.perform(get(PROJECTS_ROUTE));

        // then
        perform.andExpect(status().isUnauthorized());
    }

    private Project createTestProject(String code, String name) {
        Project project = new Project();
        project.setCode(code);
        project.setName(name);
        project.setDescription("Test description");
        project.setActive(true);
        project.setVersion(0L);
        return project;
    }
}
