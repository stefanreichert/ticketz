package net.wickedshell.ticketz.adapter.jpa.persistence;

import jakarta.inject.Inject;
import net.wickedshell.ticketz.adapter.jpa.repository.UserRepository;
import net.wickedshell.ticketz.service.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class UserJPAPersistenceImplTest {

    @Inject
    private UserRepository userRepository;

    private UserJPAPersistenceImpl unitUnderTest;

    @BeforeEach
    public void setupTest() {
        // setup unit under test
        ModelMapper mapper = new ModelMapper();
        unitUnderTest = new UserJPAPersistenceImpl(mapper, userRepository);
    }

    @Test
    void testCreateUser_success() {
        // given
        User user = new User();
        user.setEmail("test2@us.er");
        user.setFirstname("Test");
        user.setLastname("User");

        // when
        User user_created = unitUnderTest.create(user);

        // then
        assertNotNull(user_created);
        assertEquals(0, user_created.getVersion());
        assertEquals("test2@us.er", user_created.getEmail());
        assertEquals("Test", user_created.getFirstname());
        assertEquals("User", user_created.getLastname());
    }

    @Test
    void testUpdateUser_success() {
        // given
        User user = unitUnderTest.loadByEmail("test@us.er");
        user.setFirstname("Test Update");
        user.setLastname("User Update");

        // when
        User user_update = unitUnderTest.update(user);

        // then
        assertNotNull(user_update);
        assertEquals("Test Update", user_update.getFirstname());
        assertEquals("User Update", user_update.getLastname());
    }
}