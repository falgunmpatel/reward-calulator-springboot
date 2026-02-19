package com.charter.rewardcalculator.service;

import com.charter.rewardcalculator.dto.CustomerRewardSummaryDTO;
import com.charter.rewardcalculator.dto.PagedRewardSummaryDTO;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Service interface for computing customer reward summaries. */
public interface RewardService {

    /** Returns paginated reward summaries for all customers, filtered by optional date range. */
    PagedRewardSummaryDTO getRewardsPaged(Pageable pageable, LocalDate from, LocalDate to);

    /** Returns the reward summary for a single customer, filtered by optional date range. */
    CustomerRewardSummaryDTO getRewardsForCustomer(Long customerId, LocalDate from, LocalDate to);

    /**
     * Calculates reward points for a transaction amount (cents truncated).
     * $0-$50 = 0 pts, $50-$100 = 1 pt/dollar over $50, over $100 = 50 pts + 2 pts/dollar over $100.
     */
    long calculatePoints(BigDecimal amount);
}
