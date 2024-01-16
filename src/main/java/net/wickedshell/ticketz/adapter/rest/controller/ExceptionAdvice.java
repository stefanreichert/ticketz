package net.wickedshell.ticketz.adapter.rest.controller;

import net.wickedshell.ticketz.port.persistence.exception.ObjectNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Void> handleAccessDeniedException(AccessDeniedException exception) {
        logger.error(exception.getMessage());
        return ResponseEntity.status(HttpStatusCode.valueOf(401)).build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialsException(BadCredentialsException exception) {
        logger.error(exception.getMessage());
        return ResponseEntity.status(HttpStatusCode.valueOf(401)).build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Void> handleException(DataIntegrityViolationException exception) {
        logger.error("Constraint Violation");
        return ResponseEntity.status(HttpStatusCode.valueOf(409)).build();
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<Void> handleObjectNotFoundException(ObjectNotFoundException exception) {
        logger.error(exception.getMessage());
        return ResponseEntity.status(HttpStatusCode.valueOf(404)).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleException(Exception exception) {
        logger.error(exception.getMessage(), exception);
        return ResponseEntity.internalServerError().build();
    }
}
