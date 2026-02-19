package com.example.rewardcalculator.service;

import com.example.rewardcalculator.dto.CustomerRewardSummaryDTO;
import com.example.rewardcalculator.exception.CustomerNotFoundException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service contract for computing customer reward points.
 */
public interface RewardService {

    /**
     * Returns reward summaries for every customer in the system.
     *
     * @return list of reward summaries, one per customer
     */
    List<CustomerRewardSummaryDTO> getRewardsForAllCustomers();

    /**
     * Returns the reward summary for a single customer.
     *
     * @param customerId the customer's primary key
     * @return reward summary for that customer
     * @throws CustomerNotFoundException if no customer exists with the given id
     */
    CustomerRewardSummaryDTO getRewardsForCustomer(Long customerId);

    /**
     * Calculates reward points for a single transaction amount.
     * Cents are truncated; only the integer dollar portion is used.
     *
     * <ul>
     *   <li>$0–$50: 0 points</li>
     *   <li>$50.01–$100: 1 point per dollar over $50</li>
     *   <li>Over $100: 50 points (the $50–$100 band) + 2 points per dollar over $100</li>
     * </ul>
     *
     * @param amount the transaction amount; must be non-negative
     * @return reward points earned (always &ge; 0)
     */
    long calculatePoints(BigDecimal amount);
}


