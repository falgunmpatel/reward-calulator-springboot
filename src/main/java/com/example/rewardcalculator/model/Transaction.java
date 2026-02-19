package com.example.rewardcalculator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity representing a customer purchase transaction.
 */
@Entity
@Data
public class Transaction {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The customer who made this transaction. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    /**
     * Purchase amount in dollars.
     * Must be a positive value; negative amounts are rejected with a 400 error.
     */
    @NotNull
    @Positive
    private BigDecimal amount;

    /** Date the transaction occurred. */
    @NotNull
    private LocalDate transactionDate;
}
