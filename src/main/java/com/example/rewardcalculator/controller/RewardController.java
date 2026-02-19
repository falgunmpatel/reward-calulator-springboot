package com.example.rewardcalculator.controller;

import com.example.rewardcalculator.dto.CustomerRewardSummaryDTO;
import com.example.rewardcalculator.service.RewardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing reward-point calculation endpoints.
 */
@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    /**
     * Returns reward summaries for all customers.
     *
     * @return list of reward summaries
     */
    @GetMapping
    public List<CustomerRewardSummaryDTO> getAllRewards() {
        return rewardService.getRewardsForAllCustomers();
    }

    /**
     * Returns the reward summary for a single customer.
     *
     * @param customerId the customer's primary key
     * @return reward summary for that customer
     */
    @GetMapping("/{customerId}")
    public CustomerRewardSummaryDTO getCustomerReward(@PathVariable Long customerId) {
        return rewardService.getRewardsForCustomer(customerId);
    }
}
