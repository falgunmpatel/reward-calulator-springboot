package com.charter.rewardcalculator.dto;

/** Reward points earned by a customer in a specific month. */
public record MonthlyRewardDTO(int year, String month, long points) {
}
