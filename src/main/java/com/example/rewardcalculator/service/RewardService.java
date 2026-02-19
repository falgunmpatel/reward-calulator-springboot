package com.example.rewardcalculator.service;

import com.example.rewardcalculator.dto.CustomerRewardSummaryDTO;
import com.example.rewardcalculator.exception.CustomerNotFoundException;

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
}
