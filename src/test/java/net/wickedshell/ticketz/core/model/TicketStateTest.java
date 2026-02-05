package net.wickedshell.ticketz.core.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TicketStateTest {

    @Test
    void testGetPermittedSuccessors_returnsExpectedMapping() {
        // given
        TicketState created = TicketState.CREATED;
        TicketState inProgress = TicketState.IN_PROGRESS;
        TicketState fixed = TicketState.FIXED;
        TicketState rejected = TicketState.REJECTED;
        TicketState reopened = TicketState.REOPENED;
        TicketState closed = TicketState.CLOSED;

        // when
        Set<TicketState> createdSuccessors = created.getPermittedSuccessors();
        Set<TicketState> inProgressSuccessors = inProgress.getPermittedSuccessors();
        Set<TicketState> fixedSuccessors = fixed.getPermittedSuccessors();
        Set<TicketState> rejectedSuccessors = rejected.getPermittedSuccessors();
        Set<TicketState> reopenedSuccessors = reopened.getPermittedSuccessors();
        Set<TicketState> closedSuccessors = closed.getPermittedSuccessors();

        // then
        assertEquals(Set.of(TicketState.IN_PROGRESS), createdSuccessors);
        assertEquals(Set.of(TicketState.FIXED, TicketState.REJECTED), inProgressSuccessors);
        assertEquals(Set.of(TicketState.REOPENED, TicketState.CLOSED), fixedSuccessors);
        assertEquals(Set.of(TicketState.REOPENED, TicketState.CLOSED), rejectedSuccessors);
        assertEquals(Set.of(TicketState.IN_PROGRESS), reopenedSuccessors);
        assertEquals(Set.of(), closedSuccessors);
    }

    @Test
    void testCheckIsPermittedSuccessor_isConsistentWithGetPermittedSuccessors() {
        // given
        TicketState[] allStates = TicketState.values();

        // when / then
        for (TicketState state : allStates) {
            Set<TicketState> expectedSuccessors = state.getPermittedSuccessors();
            for (TicketState candidate : allStates) {
                boolean expected = expectedSuccessors.contains(candidate);

                boolean actual = state.checkIsPermittedSuccessor(candidate);

                assertEquals(expected, actual, () -> "Mismatch for " + state + " -> " + candidate);
            }
        }
    }

    @Test
    void testGetPermittedSuccessors_returnsUnmodifiableSet() {
        // given
        TicketState state = TicketState.CREATED;

        // when
        Set<TicketState> successors = state.getPermittedSuccessors();

        // then
        assertThrows(UnsupportedOperationException.class, () -> successors.add(TicketState.CREATED));
    }
}
