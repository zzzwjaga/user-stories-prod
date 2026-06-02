package com.prod.user_stories_prod.config.advice;

import com.prod.user_stories_prod.exseptions.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import com.prod.user_stories_prod.responses.ErrorResponce;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponce> handleValidation(ValidationException ex) {
        ErrorResponce error = new ErrorResponce("VALIDATION_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponce> handleIllegalState(IllegalStateException ex) {
        ErrorResponce error = new ErrorResponce("ILLEGAL_STATE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponce> handleNotFound(NoSuchElementException ex) {
        ErrorResponce error = new ErrorResponce("NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponce> handleAll(Exception ex) {
        ErrorResponce error = new ErrorResponce("INTERNAL_ERROR", "Что-то пошло не так");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
