package com.example.rewardcalculator.dto;

/**
 * Immutable DTO representing reward points earned by a customer in a single calendar month.
 *
 * @param year   calendar year (e.g. 2024)
 * @param month  month name in uppercase (e.g. "JANUARY") — never hardcoded, derived from transaction data
 * @param points total reward points earned in this month
 */
public record MonthlyRewardDTO(int year, String month, long points) {
}

