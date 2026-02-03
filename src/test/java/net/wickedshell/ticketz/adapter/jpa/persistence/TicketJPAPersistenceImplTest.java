package net.wickedshell.ticketz.adapter.jpa.persistence;

import jakarta.inject.Inject;
import net.wickedshell.ticketz.adapter.jpa.converter.ProjectToProjectEntityConverter;
import net.wickedshell.ticketz.adapter.jpa.converter.UserToUserEntityConverter;
import net.wickedshell.ticketz.adapter.jpa.repository.ProjectRepository;
import net.wickedshell.ticketz.adapter.jpa.repository.TicketRepository;
import net.wickedshell.ticketz.adapter.jpa.repository.UserRepository;
import net.wickedshell.ticketz.service.model.Project;
import net.wickedshell.ticketz.service.model.Ticket;
import net.wickedshell.ticketz.service.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;

import static net.wickedshell.ticketz.service.model.TicketState.CREATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ContextConfiguration(classes = {TicketJPAPersistenceImplTest.TestConfig.class})
class TicketJPAPersistenceImplTest {

    @Inject
    private TicketRepository ticketRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private ProjectRepository projectRepository;

    private TicketJPAPersistenceImpl unitUnderTest;

    @BeforeEach
    public void setupTest() {
        // setup unit under test
        UserToUserEntityConverter userConverter = new UserToUserEntityConverter(userRepository);
        ProjectToProjectEntityConverter projectConverter = new ProjectToProjectEntityConverter(projectRepository);
        ModelMapper mapper = new ModelMapper();
        mapper.addConverter(userConverter);
        mapper.addConverter(projectConverter);
        unitUnderTest = new TicketJPAPersistenceImpl(mapper, ticketRepository);
    }

    @Test
    void testCreateTicket_success() {
        // given
        User author = new User();
        author.setEmail("test@us.er");

        Project project = new Project();
        project.setCode("DEFAULT");

        Ticket ticket = new Ticket();
        ticket.setTicketNumber("test create");
        ticket.setTitle("Test Ticket Title");
        ticket.setState(CREATED);
        ticket.setAuthor(author);
        ticket.setProject(project);

        // when
        Ticket ticket_created = unitUnderTest.create(ticket);

        // then
        assertNotNull(ticket_created);
        assertEquals(0, ticket_created.getVersion());
        assertEquals("test create", ticket_created.getTicketNumber());
        assertEquals("Test Ticket Title", ticket_created.getTitle());
        assertEquals(CREATED, ticket_created.getState());
        assertEquals("test@us.er", ticket_created.getAuthor().getEmail());
        assertEquals("DEFAULT", ticket_created.getProject().getCode());
    }

    @Test
    void testUpdateTicket_success() {
        // given
        User editor = new User();
        editor.setEmail("test@us.er");

        Ticket ticket = unitUnderTest.loadByTicketNumber("test_ticket");
        ticket.setTitle("New Title");
        ticket.setEditor(editor);

        // when
        Ticket ticket_update = unitUnderTest.update(ticket);

        // then
        assertNotNull(ticket_update);
        assertEquals("test_ticket", ticket_update.getTicketNumber());
        assertEquals("New Title", ticket_update.getTitle());
        assertEquals(CREATED, ticket_update.getState());
        assertEquals("test@us.er", ticket_update.getAuthor().getEmail());
        assertEquals("test@us.er", ticket_update.getEditor().getEmail());
    }

    @TestConfiguration
    static class TestConfig {

    }
}