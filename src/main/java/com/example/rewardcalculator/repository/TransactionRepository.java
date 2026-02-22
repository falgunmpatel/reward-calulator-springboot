package com.example.rewardcalculator.repository;

import com.example.rewardcalculator.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for {@link Transaction} entities.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds all transactions belonging to a specific customer.
     *
     * @param customerId the customer's primary key
     * @return list of transactions for that customer, possibly empty
     */
    List<Transaction> findByCustomerId(Long customerId);

    /**
     * Finds all transactions for a customer whose transaction date falls within
     * the given inclusive range.
     *
     * @param customerId the customer's primary key
     * @param from       start date (inclusive)
     * @param to         end date (inclusive)
     * @return filtered list of transactions, possibly empty
     */
    List<Transaction> findByCustomerIdAndTransactionDateBetween(
            Long customerId, LocalDate from, LocalDate to);

    /**
     * Finds all transactions across all customers whose transaction date falls
     * within the given inclusive range.
     *
     * @param from start date (inclusive)
     * @param to   end date (inclusive)
     * @return filtered list of transactions, possibly empty
     */
    List<Transaction> findByTransactionDateBetween(LocalDate from, LocalDate to);
}
