package com.example.rewardcalculator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO representing reward points earned by a customer in a single calendar month.
 */
@Data
@AllArgsConstructor
public class MonthlyRewardDTO {

    /** Calendar year (e.g. 2024). */
    private int year;

    /** Month name in uppercase (e.g. "JANUARY"). */
    private String month;

    /** Total reward points earned in this month. */
    private long points;
}
