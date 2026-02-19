package com.charter.rewardcalculator.repository;

import com.charter.rewardcalculator.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/** JPA repository for Transaction entities with customer and date-based query methods. */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /** Returns all transactions for the given customer. */
    List<Transaction> findByCustomerId(Long customerId);

    /** Returns transactions for the given customer within an inclusive date range. */
    List<Transaction> findByCustomerIdAndTransactionDateBetween(Long customerId, LocalDate from, LocalDate to);

    /** Returns transactions for the given customer on or after the given date. */
    List<Transaction> findByCustomerIdAndTransactionDateGreaterThanEqual(Long customerId, LocalDate from);

    /** Returns transactions for the given customer on or before the given date. */
    List<Transaction> findByCustomerIdAndTransactionDateLessThanEqual(Long customerId, LocalDate to);
}
