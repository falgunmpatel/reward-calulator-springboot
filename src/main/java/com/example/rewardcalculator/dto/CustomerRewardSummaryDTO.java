package com.example.rewardcalculator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * DTO representing the full reward summary for a single customer across all months.
 */
@Data
@AllArgsConstructor
public class CustomerRewardSummaryDTO {

    /** The customer's primary key. */
    private Long customerId;

    /** The customer's full name. */
    private String customerName;

    /** Reward points broken down by month, sorted chronologically. */
    private List<MonthlyRewardDTO> monthlyRewards;

    /** Sum of all monthly reward points. */
    private long totalPoints;
}
