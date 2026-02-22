package com.example.rewardcalculator.service;

import com.example.rewardcalculator.dto.CustomerRewardSummaryDTO;
import com.example.rewardcalculator.dto.PagedRewardSummaryDTO;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RewardService {

    List<CustomerRewardSummaryDTO> getRewardsForAllCustomers();

    PagedRewardSummaryDTO getRewardsPaged(Pageable pageable, LocalDate from, LocalDate to);

    CustomerRewardSummaryDTO getRewardsForCustomer(Long customerId, LocalDate from, LocalDate to);

    long calculatePoints(BigDecimal amount);
}
