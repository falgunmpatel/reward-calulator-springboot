package com.charter.rewardcalculator.dto;

/** Structured error response returned by the API. */
public record ErrorResponseDTO(int status, String error, String message) {
}
