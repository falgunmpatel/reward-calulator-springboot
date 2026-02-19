package com.example.rewardcalculator.exception;

/**
 * Thrown when a customer with the requested ID does not exist in the system.
 */
public class CustomerNotFoundException extends RuntimeException {

    /**
     * @param customerId the ID that was not found
     */
    public CustomerNotFoundException(Long customerId) {
        super("Customer not found with id: " + customerId);
    }
}
