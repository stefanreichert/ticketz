package net.wickedshell.ticketz.adapter.rest.controller;

import jakarta.inject.Inject;
import net.wickedshell.ticketz.TestConfig;
import net.wickedshell.ticketz.adapter.AuthenticationConfiguration;
import net.wickedshell.ticketz.adapter.rest.RestAdapterConfiguration;
import net.wickedshell.ticketz.service.model.Ticket;
import net.wickedshell.ticketz.service.model.TicketState;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.access.ProjectService;
import net.wickedshell.ticketz.service.port.access.TicketService;
import net.wickedshell.ticketz.service.port.access.UserService;
import net.wickedshell.ticketz.service.port.driven.persistence.exception.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

@WebMvcTest(RestTicketController.class)
@ContextConfiguration(classes = {AuthenticationConfiguration.class, RestAdapterConfiguration.class, TestConfig.class})
class RestTicketControllerTest {

    private static final String TICKETS_ROUTE = "/api/tickets";
    private static final String TICKET_REQUEST = """
            {
                "ticketNumber": "%s",
                "title": "%s",
                "description": "%s",
                "author": {"email": "%s", "firstname": "Test", "lastname": "User"},
                "state": "%s",
                "version": %d
            }
            """;

    @Inject
    private MockMvc mvc;
    @Inject
    private WebApplicationContext context;
    @MockBean
    private TicketService ticketService;
    @MockBean
    private UserService userService;
    @MockBean
    private ProjectService projectService;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "API")
    void testAllTickets_returnsTicketList() throws Exception {
        // given
        Ticket ticket1 = createTestTicket("TICKETZ-1", "First Ticket");
        Ticket ticket2 = createTestTicket("TICKETZ-2", "Second Ticket");
        when(ticketService.findAll()).thenReturn(List.of(ticket1, ticket2));

        // when
        ResultActions perform = mvc.perform(get(TICKETS_ROUTE));

        // then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].ticketNumber", is("TICKETZ-1")))
                .andExpect(jsonPath("$[1].ticketNumber", is("TICKETZ-2")));
    }

    @Test
    @WithMockUser(roles = "API")
    void testAllTickets_emptyList() throws Exception {
        // given
        when(ticketService.findAll()).thenReturn(List.of());

        // when
        ResultActions perform = mvc.perform(get(TICKETS_ROUTE));

        // then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "API")
    void testOneTicket_found() throws Exception {
        // given
        Ticket ticket = createTestTicket("TICKETZ-1", "Test Ticket");
        when(ticketService.loadByTicketNumber("TICKETZ-1")).thenReturn(ticket);

        // when
        ResultActions perform = mvc.perform(get(TICKETS_ROUTE + "/TICKETZ-1"));

        // then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketNumber", is("TICKETZ-1")))
                .andExpect(jsonPath("$.title", is("Test Ticket")))
                .andExpect(jsonPath("$.state", is("CREATED")));
    }

    @Test
    @WithMockUser(roles = "API")
    void testOneTicket_notFound_returns404() throws Exception {
        // given
        when(ticketService.loadByTicketNumber("UNKNOWN")).thenThrow(new ObjectNotFoundException("Ticket not found: UNKNOWN"));

        // when
        ResultActions perform = mvc.perform(get(TICKETS_ROUTE + "/UNKNOWN"));

        // then
        perform.andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "API")
    void testCreate_success_returns201WithLocation() throws Exception {
        // given
        Ticket ticket = createTestTicket("TICKETZ-1", "New Ticket");
        String requestBody = String.format(TICKET_REQUEST, "TICKETZ-1", "New Ticket", "Description", "test@us.er", "CREATED", 0);
        when(ticketService.create(any(Ticket.class))).thenReturn(ticket);

        // when
        ResultActions perform = mvc.perform(post(TICKETS_ROUTE)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        perform.andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tickets/TICKETZ-1"));
    }

    @Test
    @WithMockUser(roles = "API")
    void testUpdate_success_returns204() throws Exception {
        // given
        Ticket ticket = createTestTicket("TICKETZ-1", "Updated Title");
        String requestBody = String.format(TICKET_REQUEST, "TICKETZ-1", "Updated Title", "Updated Description", "test@us.er", "CREATED", 1);
        when(ticketService.update(any(Ticket.class))).thenReturn(ticket);

        // when
        ResultActions perform = mvc.perform(put(TICKETS_ROUTE + "/TICKETZ-1")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        perform.andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "API")
    void testUpdate_ticketNumberMismatch_returns400() throws Exception {
        // given
        String requestBody = String.format(TICKET_REQUEST, "TICKETZ-2", "Some Title", "Description", "test@us.er", "CREATED", 1);

        // when
        ResultActions perform = mvc.perform(put(TICKETS_ROUTE + "/TICKETZ-1")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        perform.andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAllTickets_withoutApiRole_returns403() throws Exception {
        // when
        ResultActions perform = mvc.perform(get(TICKETS_ROUTE));

        // then
        perform.andExpect(status().isForbidden());
    }

    @Test
    void testAllTickets_unauthenticated_returns401() throws Exception {
        // when
        ResultActions perform = mvc.perform(get(TICKETS_ROUTE));

        // then
        perform.andExpect(status().isUnauthorized());
    }

    private Ticket createTestTicket(String ticketNumber, String title) {
        User author = new User();
        author.setEmail("test@us.er");
        author.setFirstname("Test");
        author.setLastname("User");

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(ticketNumber);
        ticket.setTitle(title);
        ticket.setDescription("Test description");
        ticket.setState(TicketState.CREATED);
        ticket.setAuthor(author);
        ticket.setVersion(0L);
        return ticket;
    }
}
