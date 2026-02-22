package com.example.rewardcalculator.exception;

import com.example.rewardcalculator.dto.ErrorResponseDTO;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleNotFound(CustomerNotFoundException ex) {
        return errorBody(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + " " + cv.getMessage())
                .findFirst()
                .orElse("Invalid request parameter");
        return errorBody(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return errorBody(HttpStatus.BAD_REQUEST, "Invalid parameter: " + ex.getName());
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleInvalidDateRange(InvalidDateRangeException ex) {
        return errorBody(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDTO handleGeneric(Exception ex) {
        return errorBody(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ErrorResponseDTO errorBody(HttpStatus status, String message) {
        return new ErrorResponseDTO(status.value(), status.getReasonPhrase(), message);
    }
}

