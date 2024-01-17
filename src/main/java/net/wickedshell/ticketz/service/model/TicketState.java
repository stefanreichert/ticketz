package net.wickedshell.ticketz.service.model;

import java.util.stream.IntStream;

public enum TicketState {

    CREATED(new int[]{1}),
    IN_PROGRESS(new int[]{2, 3}),
    FIXED(new int[]{4, 5}),
    REJECTED(new int[]{4, 5}),
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
