package net.wickedshell.ticketz.adapter.rest.controller;

import jakarta.persistence.OptimisticLockException;
import net.wickedshell.ticketz.service.exception.AuthenticationException;
import net.wickedshell.ticketz.service.exception.ValidationException;
import net.wickedshell.ticketz.service.port.driven.persistence.exception.ObjectNotFoundException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialsException(BadCredentialsException exception) {
        logger.error(exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Void> handleAuthenticationException(AuthenticationException exception) {
        logger.error(exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Void> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        logger.error(exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<Void> handleOptimisticLockException(OptimisticLockException exception) {
        logger.error(exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<Void> handleObjectNotFoundException(ObjectNotFoundException exception) {
        logger.error(exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Void> handleValidationException(ValidationException exception) {
        logger.error(exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Void> handleException(Throwable throwable) {
        logger.error(throwable.getMessage(), throwable);
        return ResponseEntity.internalServerError().build();
    }
}
