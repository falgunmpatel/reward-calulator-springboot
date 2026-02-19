package com.charter.rewardcalculator.exception;

import java.time.LocalDate;

/** Thrown when the 'from' date is after the 'to' date in a date range filter. */
public class InvalidDateRangeException extends RuntimeException {

    /** Constructs the exception with a message showing the invalid date range. */
    public InvalidDateRangeException(LocalDate from, LocalDate to) {
        super("'from' date (" + from + ") must not be after 'to' date (" + to + ")");
    }
}
