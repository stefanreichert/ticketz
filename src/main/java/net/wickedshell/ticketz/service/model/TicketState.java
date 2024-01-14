package net.wickedshell.ticketz.service.model;

import java.util.stream.IntStream;

public enum TicketState {

    CREATED(new int[]{0}),
    IN_PROGRESS(new int[]{1, 2}),
    REJECTED(new int[]{3, 4}),
    REOPENED(new int[]{1}),
    CLOSED(new int[0]);

    private final int[] permittedSucessorOrdinals;

    TicketState(int[] permittedSucessorOrdinals) {
        this.permittedSucessorOrdinals = permittedSucessorOrdinals;
    }

    public boolean checkIsPermittedSuccessor(TicketState ticketState) {
        return IntStream.of(permittedSucessorOrdinals).anyMatch(x -> x == ticketState.ordinal());
    }
}
