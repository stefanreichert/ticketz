package net.wickedshell.ticketz.core;

import net.wickedshell.ticketz.core.exception.ValidationException;
import net.wickedshell.ticketz.core.model.Project;
import net.wickedshell.ticketz.core.model.Ticket;
import net.wickedshell.ticketz.core.model.TicketState;
import net.wickedshell.ticketz.core.model.User;
import net.wickedshell.ticketz.core.port.access.CommentService;
import net.wickedshell.ticketz.core.port.access.ProjectService;
import net.wickedshell.ticketz.core.port.access.UserService;
import net.wickedshell.ticketz.core.port.driven.persistence.TicketPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.wickedshell.ticketz.core.model.TicketState.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketPersistence ticketPersistence;
    @Mock
    private UserService userService;
    @Mock
    private CommentService commentService;
    @Mock
    private ProjectService projectService;

    private TicketServiceImpl unitUnderTest;

    private User author;
    private User editor;
    private User otherUser;

    @BeforeEach
    void setUp() {
        unitUnderTest = new TicketServiceImpl(ticketPersistence, userService, commentService, projectService);

        author = createUser("author@test.com", "Author", "User");
        editor = createUser("editor@test.com", "Editor", "User");
        otherUser = createUser("other@test.com", "Other", "User");
    }

    @Nested
    class EvaluateCanBeEdited {

        @Test
        void testInactiveProject_noOneCanEdit() {
            // given
            Ticket ticket = createTicket(CREATED, author, null);
            ticket.getProject().setActive(false);

            // when
            boolean canEdit = unitUnderTest.evaluateCanBeEdited(ticket);

            // then
            assertFalse(canEdit);
        }

        @Test
        void testClosed_noOneCanEdit() {
            // given
            Ticket ticket = createTicket(CLOSED, author, editor);
            // Note: getCurrentUser() is not called for CLOSED state - returns early

            // when
            boolean canEdit = unitUnderTest.evaluateCanBeEdited(ticket);

            // then
            assertFalse(canEdit);
        }

        @Test
        void testFixed_authorCanEdit() {
            // given
            Ticket ticket = createTicket(FIXED, author, editor);
            when(userService.getCurrentUser()).thenReturn(author);

            // when
            boolean canEdit = unitUnderTest.evaluateCanBeEdited(ticket);

            // then
            assertTrue(canEdit);
        }

        @Test
        void testFixed_nonAuthorCannotEdit() {
            // given
            Ticket ticket = createTicket(FIXED, author, editor);
            when(userService.getCurrentUser()).thenReturn(otherUser);

            // when
            boolean canEdit = unitUnderTest.evaluateCanBeEdited(ticket);

            // then
            assertFalse(canEdit);
        }

        @Test
        void testRejected_authorCanEdit() {
            // given
            Ticket ticket = createTicket(REJECTED, author, editor);
            when(userService.getCurrentUser()).thenReturn(author);

            // when
            boolean canEdit = unitUnderTest.evaluateCanBeEdited(ticket);

            // then
            assertTrue(canEdit);
        }

        @Test
        void testRejected_nonAuthorCannotEdit() {
            // given
            Ticket ticket = createTicket(REJECTED, author, editor);
            when(userService.getCurrentUser()).thenReturn(otherUser);

            // when
            boolean canEdit = unitUnderTest.evaluateCanBeEdited(ticket);

            // then
            assertFalse(canEdit);
        }

        @Test
        void testInProgress_editorCanEdit() {
            // given
            Ticket ticket = createTicket(IN_PROGRESS, author, editor);
            when(userService.getCurrentUser()).thenReturn(editor);

            // when
            boolean canEdit = unitUnderTest.evaluateCanBeEdited(ticket);

            // then
            assertTrue(canEdit);
        }

        @Test
        void testInProgress_nonEditorCannotEdit() {
            // given
            Ticket ticket = createTicket(IN_PROGRESS, author, editor);
            when(userService.getCurrentUser()).thenReturn(author);

            // when
            boolean canEdit = unitUnderTest.evaluateCanBeEdited(ticket);

            // then
            assertFalse(canEdit);
        }

        @Test
        void testCreated_anyoneCanEdit() {
            // given
            Ticket ticket = createTicket(CREATED, author, null);
            // Note: getCurrentUser() is not called for CREATED state - returns true directly

            // when
            boolean canEdit = unitUnderTest.evaluateCanBeEdited(ticket);

            // then
            assertTrue(canEdit);
        }

        @Test
        void testReopened_anyoneCanEdit() {
            // given
            Ticket ticket = createTicket(REOPENED, author, null);
            // Note: getCurrentUser() is not called for REOPENED state - returns true directly

            // when
            boolean canEdit = unitUnderTest.evaluateCanBeEdited(ticket);

            // then
            assertTrue(canEdit);
        }
    }

    @Nested
    class Update_StateTransitionValidation {

        @Test
        void testValidTransition_createdToInProgress() {
            // given
            when(userService.getCurrentUser()).thenReturn(author);

            Ticket existingTicket = createTicket(CREATED, author, null);
            Ticket updatedTicket = createTicket(IN_PROGRESS, author, null);

            when(ticketPersistence.loadByTicketNumber("TICKETZ-1")).thenReturn(existingTicket);
            when(ticketPersistence.update(any(Ticket.class))).thenReturn(updatedTicket);

            // when / then - no exception
            assertDoesNotThrow(() -> unitUnderTest.update(updatedTicket));
        }

        @Test
        void testInvalidTransition_createdToFixed_throwsException() {
            // given
            Ticket existingTicket = createTicket(CREATED, author, null);
            Ticket updatedTicket = createTicket(FIXED, author, null);

            when(ticketPersistence.loadByTicketNumber("TICKETZ-1")).thenReturn(existingTicket);
            // Note: getCurrentUser() not called - evaluateCanBeEdited returns true for CREATED,
            // then validateStateChange throws before checking author

            // when / then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> unitUnderTest.update(updatedTicket));
            assertTrue(exception.getMessage().contains("not permitted"));
        }

        @Test
        void testInvalidTransition_createdToClosed_throwsException() {
            // given
            Ticket existingTicket = createTicket(CREATED, author, null);
            Ticket updatedTicket = createTicket(CLOSED, author, null);

            when(ticketPersistence.loadByTicketNumber("TICKETZ-1")).thenReturn(existingTicket);
            // Note: getCurrentUser() not called - evaluateCanBeEdited returns true for CREATED,
            // then validateStateChange throws before checking author

            // when / then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> unitUnderTest.update(updatedTicket));
            assertTrue(exception.getMessage().contains("not permitted"));
        }

        @Test
        void testValidTransition_inProgressToFixed() {
            // given
            when(userService.getCurrentUser()).thenReturn(author);

            Ticket existingTicket = createTicket(IN_PROGRESS, author, author);
            Ticket updatedTicket = createTicket(FIXED, author, author);

            when(ticketPersistence.loadByTicketNumber("TICKETZ-1")).thenReturn(existingTicket);
            when(ticketPersistence.update(any(Ticket.class))).thenReturn(updatedTicket);

            // when / then - no exception
            assertDoesNotThrow(() -> unitUnderTest.update(updatedTicket));
        }

        @Test
        void testValidTransition_inProgressToRejected() {
            // given
            when(userService.getCurrentUser()).thenReturn(author);

            Ticket existingTicket = createTicket(IN_PROGRESS, author, author);
            Ticket updatedTicket = createTicket(REJECTED, author, author);

            when(ticketPersistence.loadByTicketNumber("TICKETZ-1")).thenReturn(existingTicket);
            when(ticketPersistence.update(any(Ticket.class))).thenReturn(updatedTicket);

            // when / then - no exception
            assertDoesNotThrow(() -> unitUnderTest.update(updatedTicket));
        }
    }

    @Nested
    class Update_AuthorOnlyOperations {

        @Test
        void testClose_byAuthor_succeeds() {
            // given
            when(userService.getCurrentUser()).thenReturn(author);

            Ticket existingTicket = createTicket(FIXED, author, editor);
            Ticket updatedTicket = createTicket(CLOSED, author, editor);

            when(ticketPersistence.loadByTicketNumber("TICKETZ-1")).thenReturn(existingTicket);
            when(ticketPersistence.update(any(Ticket.class))).thenReturn(updatedTicket);

            // when / then - no exception
            assertDoesNotThrow(() -> unitUnderTest.update(updatedTicket));
        }

        @Test
        void testClose_byNonAuthor_throwsException() {
            // given
            when(userService.getCurrentUser()).thenReturn(otherUser);

            Ticket existingTicket = createTicket(FIXED, author, editor);
            Ticket updatedTicket = createTicket(CLOSED, author, editor);

            when(ticketPersistence.loadByTicketNumber("TICKETZ-1")).thenReturn(existingTicket);

            // when / then
            // Note: For FIXED tickets, only author can edit - so non-author is blocked by evaluateCanBeEdited
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> unitUnderTest.update(updatedTicket));
            assertTrue(exception.getMessage().contains("cannot be edited"));
        }

        @Test
        void testReopen_byAuthor_succeeds() {
            // given
            when(userService.getCurrentUser()).thenReturn(author);

            Ticket existingTicket = createTicket(REJECTED, author, editor);
            Ticket updatedTicket = createTicket(REOPENED, author, null);

            when(ticketPersistence.loadByTicketNumber("TICKETZ-1")).thenReturn(existingTicket);
            when(ticketPersistence.update(any(Ticket.class))).thenReturn(updatedTicket);

            // when / then - no exception
            assertDoesNotThrow(() -> unitUnderTest.update(updatedTicket));
        }

        @Test
        void testReopen_byNonAuthor_throwsException() {
            // given
            when(userService.getCurrentUser()).thenReturn(otherUser);

            Ticket existingTicket = createTicket(REJECTED, author, editor);
            Ticket updatedTicket = createTicket(REOPENED, author, null);

            when(ticketPersistence.loadByTicketNumber("TICKETZ-1")).thenReturn(existingTicket);

            // when / then
            // Note: For REJECTED tickets, only author can edit - so non-author is blocked by evaluateCanBeEdited
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> unitUnderTest.update(updatedTicket));
            assertTrue(exception.getMessage().contains("cannot be edited"));
        }
    }

    @Nested
    class Update_EditorAssignment {

        @Test
        void testTransitionToInProgress_setsCurrentUserAsEditor() {
            // given
            when(userService.getCurrentUser()).thenReturn(otherUser);

            Ticket existingTicket = createTicket(CREATED, author, null);
            Ticket updatedTicket = createTicket(IN_PROGRESS, author, null);

            when(ticketPersistence.loadByTicketNumber("TICKETZ-1")).thenReturn(existingTicket);
            when(ticketPersistence.update(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Ticket result = unitUnderTest.update(updatedTicket);

            // then
            assertEquals(otherUser.getEmail(), result.getEditor().getEmail());
        }

        @Test
        void testTransitionToClosed_clearsEditor() {
            // given
            when(userService.getCurrentUser()).thenReturn(author);

            Ticket existingTicket = createTicket(FIXED, author, editor);
            Ticket updatedTicket = createTicket(CLOSED, author, editor);

            when(ticketPersistence.loadByTicketNumber("TICKETZ-1")).thenReturn(existingTicket);
            when(ticketPersistence.update(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Ticket result = unitUnderTest.update(updatedTicket);

            // then
            assertNull(result.getEditor());
        }

        @Test
        void testTransitionToReopened_clearsEditor() {
            // given
            when(userService.getCurrentUser()).thenReturn(author);

            Ticket existingTicket = createTicket(FIXED, author, editor);
            Ticket updatedTicket = createTicket(REOPENED, author, editor);

            when(ticketPersistence.loadByTicketNumber("TICKETZ-1")).thenReturn(existingTicket);
            when(ticketPersistence.update(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Ticket result = unitUnderTest.update(updatedTicket);

            // then
            assertNull(result.getEditor());
        }
    }

    @Nested
    class Create_TicketNumberGeneration {

        @Test
        void testCreate_generatesTicketNumber() {
            // given
            when(userService.getCurrentUser()).thenReturn(author);
            when(ticketPersistence.getTicketCount()).thenReturn(5L);
            when(ticketPersistence.create(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Ticket newTicket = new Ticket();
            newTicket.setTitle("New Ticket");
            newTicket.setProject(createProject());

            // when
            Ticket result = unitUnderTest.create(newTicket);

            // then
            assertEquals("TEST-6", result.getTicketNumber());
        }

        @Test
        void testCreate_setsStateToCreated() {
            // given
            when(userService.getCurrentUser()).thenReturn(author);
            when(ticketPersistence.getTicketCount()).thenReturn(0L);
            when(ticketPersistence.create(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Ticket newTicket = new Ticket();
            newTicket.setTitle("New Ticket");
            newTicket.setProject(createProject());

            // when
            Ticket result = unitUnderTest.create(newTicket);

            // then
            assertEquals(CREATED, result.getState());
        }

        @Test
        void testCreate_setsCurrentUserAsAuthor() {
            // given
            when(userService.getCurrentUser()).thenReturn(editor);
            when(ticketPersistence.getTicketCount()).thenReturn(0L);
            when(ticketPersistence.create(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Ticket newTicket = new Ticket();
            newTicket.setTitle("New Ticket");
            newTicket.setProject(createProject());

            // when
            Ticket result = unitUnderTest.create(newTicket);

            // then
            assertEquals(editor.getEmail(), result.getAuthor().getEmail());
        }
    }

    @Nested
    class ProjectValidation {

        @Test
        void testCreate_withoutProject_throwsException() {
            // given
            Ticket newTicket = new Ticket();
            newTicket.setTitle("New Ticket");
            newTicket.setProject(null);

            // when / then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> unitUnderTest.create(newTicket));
            assertTrue(exception.getMessage().contains("Project is required"));
        }

        @Test
        void testCreate_withInactiveProject_throwsException() {
            // given
            doThrow(new ValidationException("Project 'INACTIVE' is inactive"))
                    .when(projectService).validateProjectCode("INACTIVE");

            Ticket newTicket = new Ticket();
            newTicket.setTitle("New Ticket");
            Project inactiveProject = new Project();
            inactiveProject.setCode("INACTIVE");
            newTicket.setProject(inactiveProject);

            // when / then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> unitUnderTest.create(newTicket));
            assertTrue(exception.getMessage().contains("inactive"));
        }
    }

    // Helper methods

    private User createUser(String email, String firstname, String lastname) {
        User user = new User();
        user.setEmail(email);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        return user;
    }

    private Ticket createTicket(TicketState state, User author, User editor) {
        Ticket ticket = new Ticket();
        ticket.setTicketNumber("TICKETZ-1");
        ticket.setTitle("Test Ticket");
        ticket.setState(state);
        ticket.setAuthor(author);
        ticket.setEditor(editor);
        ticket.setVersion(0L);
        ticket.setProject(createProject());
        return ticket;
    }

    private Project createProject() {
        Project project = new Project();
        project.setCode("TEST");
        project.setName("Test Project");
        project.setActive(true);
        return project;
    }
}
