package com.example.rewardcalculator.exception;

import java.time.LocalDate;

/**
 * Thrown when the {@code from} date is after the {@code to} date in a date-range filter.
 */
public class InvalidDateRangeException extends RuntimeException {

    /**
     * @param from the start date supplied by the caller
     * @param to   the end date supplied by the caller
     */
    public InvalidDateRangeException(LocalDate from, LocalDate to) {
        super("'from' date (" + from + ") must not be after 'to' date (" + to + ")");
    }
}

