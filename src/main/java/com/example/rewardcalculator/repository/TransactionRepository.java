package com.example.rewardcalculator.repository;

import com.example.rewardcalculator.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
