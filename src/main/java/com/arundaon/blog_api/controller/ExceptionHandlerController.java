package com.arundaon.blog_api.controller;

import com.arundaon.blog_api.models.WebResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class ExceptionHandlerController {
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<WebResponse<String>> constraintViolationException(ConstraintViolationException cve) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(WebResponse.<String>builder().errors(cve.getMessage()).build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<WebResponse<String>> dataIntegrityViolationException(DataIntegrityViolationException dive) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(WebResponse.<String>builder().errors(dive.getMessage()).build());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<WebResponse<String>> responseStatusException(ResponseStatusException rse){
        return ResponseEntity.status(rse.getStatusCode()).body(WebResponse.<String>builder().errors(rse.getReason()).build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<WebResponse<String>> handleTypeMismatch(MethodArgumentTypeMismatchException matme) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(WebResponse.<String>builder().errors(matme.getMessage()).build());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<WebResponse<String>> handleNoHandlerFoundException(NoHandlerFoundException nhfe) {
        return ResponseEntity.status(nhfe.getStatusCode()).body(WebResponse.<String>builder().errors(nhfe.getMessage()).build());
    }
}
