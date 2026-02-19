package com.example.rewardcalculator.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * JPA entity representing a retail customer.
 */
@Entity
@Data
public class Customer {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Full name of the customer. */
    private String name;

    /** Email address of the customer. */
    private String email;
}
