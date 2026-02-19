package com.example.rewardcalculator.dto;

import java.util.List;

/**
 * Immutable DTO representing the full reward summary for a single customer across all months.
 *
 * @param customerId      the customer's primary key
 * @param customerName    the customer's full name
 * @param monthlyRewards  reward points broken down by month, sorted chronologically; never null
 * @param totalPoints     sum of all monthly reward points
 */
public record CustomerRewardSummaryDTO(
        Long customerId,
        String customerName,
        List<MonthlyRewardDTO> monthlyRewards,
        long totalPoints) {
}

