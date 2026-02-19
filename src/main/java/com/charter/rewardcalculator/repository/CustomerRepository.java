package com.charter.rewardcalculator.repository;

import com.charter.rewardcalculator.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for Customer entities. */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
