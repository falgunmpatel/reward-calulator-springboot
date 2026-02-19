package com.example.rewardcalculator.exception;

import com.example.rewardcalculator.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Centralised exception handler for all REST controllers.
 * Returns a consistent {@link ErrorResponseDTO} body for all error scenarios.
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
    public ErrorResponseDTO handleNotFound(CustomerNotFoundException ex) {
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
    public ErrorResponseDTO handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
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
    public ErrorResponseDTO handleGeneric(Exception ex) {
        return errorBody(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    /**
     * Builds a structured {@link ErrorResponseDTO} from an HTTP status and message.
     *
     * @param status  the HTTP status
     * @param message the error detail message
     * @return populated error response DTO
     */
    private ErrorResponseDTO errorBody(HttpStatus status, String message) {
        return new ErrorResponseDTO(status.value(), status.getReasonPhrase(), message);
    }
}
