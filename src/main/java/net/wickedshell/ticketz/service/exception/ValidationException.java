package net.wickedshell.ticketz.service.exception;

public class ValidationException extends ServiceException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
