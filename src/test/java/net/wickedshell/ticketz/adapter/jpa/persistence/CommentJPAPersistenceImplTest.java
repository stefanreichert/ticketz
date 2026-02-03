package net.wickedshell.ticketz.adapter.jpa.persistence;

import jakarta.inject.Inject;
import net.wickedshell.ticketz.adapter.jpa.converter.UserToUserEntityConverter;
import net.wickedshell.ticketz.adapter.jpa.repository.CommentRepository;
import net.wickedshell.ticketz.adapter.jpa.repository.TicketRepository;
import net.wickedshell.ticketz.adapter.jpa.repository.UserRepository;
import net.wickedshell.ticketz.service.model.Comment;
import net.wickedshell.ticketz.service.model.Ticket;
import net.wickedshell.ticketz.service.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CommentJPAPersistenceImplTest {

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private TicketRepository ticketRepository;

    @Inject
    private UserRepository userRepository;

    private CommentJPAPersistenceImpl unitUnderTest;

    @BeforeEach
    public void setupTest() {
        UserToUserEntityConverter userConverter = new UserToUserEntityConverter(userRepository);
        unitUnderTest = new CommentJPAPersistenceImpl(commentRepository, ticketRepository, userConverter);
    }

    @Test
    void testCreate_success() {
        // given
        User author = new User();
        author.setEmail("test@us.er");

        Comment comment = new Comment();
        comment.setText("Test comment text");
        comment.setAuthor(author);

        Ticket ticket = new Ticket();
        ticket.setTicketNumber("test_ticket");

        // when
        Comment createdComment = unitUnderTest.create(comment, ticket);

        // then
        assertNotNull(createdComment);
        assertEquals("Test comment text", createdComment.getText());
        assertEquals("test@us.er", createdComment.getAuthor().getEmail());
    }

    @Test
    void testFindByTicketNumber_emptyList() {
        // when
        List<Comment> comments = unitUnderTest.findByTicketNumber("test_ticket");

        // then
        assertNotNull(comments);
        assertTrue(comments.isEmpty());
    }

    @Test
    void testFindByTicketNumber_returnsComments() {
        // given
        User author = new User();
        author.setEmail("test@us.er");

        Comment comment = new Comment();
        comment.setText("Test comment");
        comment.setAuthor(author);

        Ticket ticket = new Ticket();
        ticket.setTicketNumber("test_ticket");

        unitUnderTest.create(comment, ticket);

        // when
        List<Comment> comments = unitUnderTest.findByTicketNumber("test_ticket");

        // then
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("Test comment", comments.get(0).getText());
    }
}
