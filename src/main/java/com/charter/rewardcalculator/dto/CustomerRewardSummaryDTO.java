package com.charter.rewardcalculator.dto;

import java.util.List;

/** Reward point summary for a single customer with monthly breakdown and total. */
public record CustomerRewardSummaryDTO(
        Long customerId,
        String customerName,
        List<MonthlyRewardDTO> monthlyRewards,
        long totalPoints) {
}
