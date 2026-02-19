package com.charter.rewardcalculator.exception;

/** Thrown when a customer is not found by ID. */
public class CustomerNotFoundException extends RuntimeException {

    /** Constructs the exception with a message containing the missing customer ID. */
    public CustomerNotFoundException(Long customerId) {
        super("Customer not found with id: " + customerId);
    }
}
