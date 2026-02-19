package com.example.rewardcalculator.dto;

/**
 * Immutable DTO representing a structured error response returned by the API.
 *
 * @param status  the HTTP status code (e.g. 404)
 * @param error   the HTTP status reason phrase (e.g. "Not Found")
 * @param message a human-readable description of the error
 */
public record ErrorResponseDTO(int status, String error, String message) {
}

