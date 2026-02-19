package com.example.rewardcalculator.repository;

import com.example.rewardcalculator.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Customer} entities.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
