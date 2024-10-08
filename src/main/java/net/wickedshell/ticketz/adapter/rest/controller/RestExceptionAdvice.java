package net.wickedshell.ticketz.adapter.rest.controller;

import jakarta.persistence.OptimisticLockException;
import net.wickedshell.ticketz.service.exception.ValidationException;
import net.wickedshell.ticketz.service.port.persistence.exception.ObjectNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(annotations = RestController.class)
public class RestExceptionAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Void> handleAccessDeniedException(AccessDeniedException exception) {
        logger.error(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatusCode.valueOf(401)).build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialsException(BadCredentialsException exception) {
        logger.error(exception.getMessage());
        return ResponseEntity.status(HttpStatusCode.valueOf(401)).build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Void> handleDataIntegrityViolationException() {
        logger.error("Constraint Violation");
        return ResponseEntity.status(HttpStatusCode.valueOf(409)).build();
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<Void> handleOptimisticLockException() {
        logger.error("Optimistic Lock");
        return ResponseEntity.status(HttpStatusCode.valueOf(409)).build();
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<Void> handleObjectNotFoundException(ObjectNotFoundException exception) {
        logger.error(exception.getMessage());
        return ResponseEntity.status(HttpStatusCode.valueOf(404)).build();
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Void> handleValidationException(ValidationException exception) {
        logger.error(exception.getMessage());
        return ResponseEntity.status(HttpStatusCode.valueOf(400)).build();
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Void> handleException(Throwable throwable) {
        logger.error(throwable.getMessage(), throwable);
        return ResponseEntity.internalServerError().build();
    }
}
