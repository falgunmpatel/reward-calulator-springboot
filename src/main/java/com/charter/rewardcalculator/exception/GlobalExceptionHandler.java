package com.charter.rewardcalculator.exception;

import com.charter.rewardcalculator.dto.ErrorResponseDTO;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Handles exceptions thrown by controllers and maps them to structured error responses. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Handles CustomerNotFoundException — returns 404. */
    @ExceptionHandler(CustomerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleNotFound(CustomerNotFoundException ex) {
        return errorBody(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Handles constraint violations on request parameters — returns 400. */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + " " + cv.getMessage())
                .findFirst()
                .orElse("Invalid request parameter");
        return errorBody(HttpStatus.BAD_REQUEST, message);
    }

    /** Handles type mismatch on request parameters — returns 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return errorBody(HttpStatus.BAD_REQUEST, "Invalid parameter: " + ex.getName());
    }

    /** Handles invalid date range — returns 400. */
    @ExceptionHandler(InvalidDateRangeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleInvalidDateRange(InvalidDateRangeException ex) {
        return errorBody(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Handles all other unexpected exceptions — returns 500. */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDTO handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return errorBody(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    /** Builds a consistent ErrorResponseDTO from the given status and message. */
    private ErrorResponseDTO errorBody(HttpStatus status, String message) {
        return new ErrorResponseDTO(status.value(), status.getReasonPhrase(), message);
    }
}
