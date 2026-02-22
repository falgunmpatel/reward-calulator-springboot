package com.example.rewardcalculator.dto;

import java.util.List;

/**
 * Immutable DTO wrapping a paginated slice of {@link CustomerRewardSummaryDTO} results.
 *
 * @param content        the reward summaries for the current page
 * @param page           current zero-based page number
 * @param size           number of items per page
 * @param totalElements  total number of customers across all pages
 * @param totalPages     total number of pages
 * @param last           {@code true} if this is the final page
 */
public record PagedRewardSummaryDTO(
        List<CustomerRewardSummaryDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last) {
}

