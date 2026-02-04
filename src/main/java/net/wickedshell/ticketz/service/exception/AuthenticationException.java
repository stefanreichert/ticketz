package net.wickedshell.ticketz.service.exception;

public class AuthenticationException extends ServiceException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
