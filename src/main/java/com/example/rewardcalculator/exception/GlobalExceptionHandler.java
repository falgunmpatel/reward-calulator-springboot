package com.example.rewardcalculator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

/**
 * Centralised exception handler for all REST controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles requests for a customer that does not exist.
     *
     * @param ex the exception carrying the missing customer id
     * @return 404 error body
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(CustomerNotFoundException ex) {
        return errorBody(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles path variable type mismatches (e.g. {@code /api/rewards/abc}).
     *
     * @param ex the type mismatch exception
     * @return 400 error body
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return errorBody(HttpStatus.BAD_REQUEST, "Invalid parameter: " + ex.getName());
    }

    /**
     * Catch-all handler for unexpected server errors.
     *
     * @param ex the unexpected exception
     * @return 500 error body
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGeneric(Exception ex) {
        return errorBody(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private Map<String, Object> errorBody(HttpStatus status, String message) {
        return Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );
    }
}
