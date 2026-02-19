package com.charter.rewardcalculator.dto;

import java.util.List;

/** Paginated list of customer reward summaries. */
public record PagedRewardSummaryDTO(
        List<CustomerRewardSummaryDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last) {
}
