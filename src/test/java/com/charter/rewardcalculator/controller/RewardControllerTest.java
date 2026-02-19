package com.charter.rewardcalculator.controller;

import com.charter.rewardcalculator.dto.CustomerRewardSummaryDTO;
import com.charter.rewardcalculator.dto.MonthlyRewardDTO;
import com.charter.rewardcalculator.dto.PagedRewardSummaryDTO;
import com.charter.rewardcalculator.exception.CustomerNotFoundException;
import com.charter.rewardcalculator.exception.InvalidDateRangeException;
import com.charter.rewardcalculator.service.RewardService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class RewardControllerTest {

    @Mock
    RewardService rewardService;

    @InjectMocks
    RewardController controller;

    @Test
    void getAllRewards_returnsOk() {
        var paged = new PagedRewardSummaryDTO(List.of(), 0, 10, 0, 0, true);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Mockito.when(rewardService.getRewardsPaged(pageable, null, null)).thenReturn(paged);

        var response = controller.getAllRewards(0, 10, null, null);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAllRewards_returnsPagedBody() {
        var summary = new CustomerRewardSummaryDTO(1L, "Alice Johnson",
                List.of(new MonthlyRewardDTO(2024, "JANUARY", 115)), 115L);
        var paged = new PagedRewardSummaryDTO(List.of(summary), 0, 10, 1, 1, true);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Mockito.when(rewardService.getRewardsPaged(pageable, null, null)).thenReturn(paged);

        var response = controller.getAllRewards(0, 10, null, null);

        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().totalElements()).isEqualTo(1);
        Assertions.assertThat(response.getBody().content().get(0).customerId()).isEqualTo(1L);
        Assertions.assertThat(response.getBody().content().get(0).customerName()).isEqualTo("Alice Johnson");
    }

    @Test
    void getAllRewards_emptyContent_returnsEmptyList() {
        var paged = new PagedRewardSummaryDTO(List.of(), 0, 10, 0, 0, true);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Mockito.when(rewardService.getRewardsPaged(pageable, null, null)).thenReturn(paged);

        var response = controller.getAllRewards(0, 10, null, null);

        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().content()).isEmpty();
        Assertions.assertThat(response.getBody().totalElements()).isZero();
    }

    @Test
    void getAllRewards_withFromAndTo_passesDatesThroughToService() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to   = LocalDate.of(2024, 1, 31);
        var paged = new PagedRewardSummaryDTO(List.of(), 0, 10, 0, 0, true);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Mockito.when(rewardService.getRewardsPaged(pageable, from, to)).thenReturn(paged);

        controller.getAllRewards(0, 10, from, to);

        Mockito.verify(rewardService).getRewardsPaged(pageable, from, to);
    }

    @Test
    void getAllRewards_withOnlyFrom_passesFromNullToToService() {
        LocalDate from = LocalDate.of(2024, 2, 1);
        var paged = new PagedRewardSummaryDTO(List.of(), 0, 10, 0, 0, true);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Mockito.when(rewardService.getRewardsPaged(pageable, from, null)).thenReturn(paged);

        controller.getAllRewards(0, 10, from, null);

        Mockito.verify(rewardService).getRewardsPaged(pageable, from, null);
    }

    @Test
    void getAllRewards_withOnlyTo_passesNullFromToService() {
        LocalDate to = LocalDate.of(2024, 1, 31);
        var paged = new PagedRewardSummaryDTO(List.of(), 0, 10, 0, 0, true);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Mockito.when(rewardService.getRewardsPaged(pageable, null, to)).thenReturn(paged);

        controller.getAllRewards(0, 10, null, to);

        Mockito.verify(rewardService).getRewardsPaged(pageable, null, to);
    }

    @Test
    void getAllRewards_customPageAndSize_buildsCorrectPageable() {
        var paged = new PagedRewardSummaryDTO(List.of(), 1, 5, 0, 0, true);
        Pageable pageable = PageRequest.of(1, 5, Sort.by("id").ascending());
        Mockito.when(rewardService.getRewardsPaged(pageable, null, null)).thenReturn(paged);

        var response = controller.getAllRewards(1, 5, null, null);

        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().page()).isEqualTo(1);
        Assertions.assertThat(response.getBody().size()).isEqualTo(5);
        Mockito.verify(rewardService).getRewardsPaged(pageable, null, null);
    }

    @Test
    void getAllRewards_serviceThrowsInvalidDateRange_propagatesException() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        LocalDate from = LocalDate.of(2024, 3, 1);
        LocalDate to   = LocalDate.of(2024, 1, 1);
        Mockito.when(rewardService.getRewardsPaged(pageable, from, to))
                .thenThrow(new InvalidDateRangeException(from, to));

        Assertions.assertThatThrownBy(() -> controller.getAllRewards(0, 10, from, to))
                .isInstanceOf(InvalidDateRangeException.class);
    }

    @Test
    void getCustomerReward_returnsOk() {
        var summary = new CustomerRewardSummaryDTO(1L, "Alice Johnson",
                List.of(new MonthlyRewardDTO(2024, "JANUARY", 115)), 115L);
        Mockito.when(rewardService.getRewardsForCustomer(1L, null, null)).thenReturn(summary);

        var response = controller.getCustomerReward(1L, null, null);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getCustomerReward_returnsCorrectBody() {
        var summary = new CustomerRewardSummaryDTO(1L, "Alice Johnson",
                List.of(new MonthlyRewardDTO(2024, "JANUARY", 115)), 115L);
        Mockito.when(rewardService.getRewardsForCustomer(1L, null, null)).thenReturn(summary);

        var response = controller.getCustomerReward(1L, null, null);

        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().customerId()).isEqualTo(1L);
        Assertions.assertThat(response.getBody().customerName()).isEqualTo("Alice Johnson");
        Assertions.assertThat(response.getBody().totalPoints()).isEqualTo(115L);
    }

    @Test
    void getCustomerReward_monthlyRewardsReturnedInOrder() {
        var monthly = List.of(
                new MonthlyRewardDTO(2024, "JANUARY", 115),
                new MonthlyRewardDTO(2024, "FEBRUARY", 250),
                new MonthlyRewardDTO(2024, "MARCH", 70)
        );
        var summary = new CustomerRewardSummaryDTO(1L, "Alice Johnson", monthly, 435L);
        Mockito.when(rewardService.getRewardsForCustomer(1L, null, null)).thenReturn(summary);

        var response = controller.getCustomerReward(1L, null, null);

        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().monthlyRewards()).hasSize(3);
        Assertions.assertThat(response.getBody().monthlyRewards().get(0).month()).isEqualTo("JANUARY");
        Assertions.assertThat(response.getBody().monthlyRewards().get(1).month()).isEqualTo("FEBRUARY");
        Assertions.assertThat(response.getBody().monthlyRewards().get(2).month()).isEqualTo("MARCH");
    }

    @Test
    void getCustomerReward_noTransactions_returnsZeroPointsAndEmptyMonthly() {
        var summary = new CustomerRewardSummaryDTO(1L, "Alice", List.of(), 0L);
        Mockito.when(rewardService.getRewardsForCustomer(1L, null, null)).thenReturn(summary);

        var response = controller.getCustomerReward(1L, null, null);

        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().monthlyRewards()).isEmpty();
        Assertions.assertThat(response.getBody().totalPoints()).isZero();
    }

    @Test
    void getCustomerReward_withFromAndTo_passesDatesThroughToService() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to   = LocalDate.of(2024, 1, 31);
        var summary = new CustomerRewardSummaryDTO(1L, "Alice", List.of(), 0L);
        Mockito.when(rewardService.getRewardsForCustomer(1L, from, to)).thenReturn(summary);

        controller.getCustomerReward(1L, from, to);

        Mockito.verify(rewardService).getRewardsForCustomer(1L, from, to);
    }

    @Test
    void getCustomerReward_withOnlyFrom_passesFromNullToToService() {
        LocalDate from = LocalDate.of(2024, 2, 1);
        var summary = new CustomerRewardSummaryDTO(1L, "Alice", List.of(), 0L);
        Mockito.when(rewardService.getRewardsForCustomer(1L, from, null)).thenReturn(summary);

        controller.getCustomerReward(1L, from, null);

        Mockito.verify(rewardService).getRewardsForCustomer(1L, from, null);
    }

    @Test
    void getCustomerReward_withOnlyTo_passesNullFromToService() {
        LocalDate to = LocalDate.of(2024, 1, 31);
        var summary = new CustomerRewardSummaryDTO(1L, "Alice", List.of(), 0L);
        Mockito.when(rewardService.getRewardsForCustomer(1L, null, to)).thenReturn(summary);

        controller.getCustomerReward(1L, null, to);

        Mockito.verify(rewardService).getRewardsForCustomer(1L, null, to);
    }

    @Test
    void getCustomerReward_fromEqualsTo_singleDayPassedToService() {
        LocalDate day = LocalDate.of(2024, 1, 15);
        var summary = new CustomerRewardSummaryDTO(1L, "Alice",
                List.of(new MonthlyRewardDTO(2024, "JANUARY", 90)), 90L);
        Mockito.when(rewardService.getRewardsForCustomer(1L, day, day)).thenReturn(summary);

        var response = controller.getCustomerReward(1L, day, day);

        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().totalPoints()).isEqualTo(90L);
        Mockito.verify(rewardService).getRewardsForCustomer(1L, day, day);
    }

    @Test
    void getCustomerReward_customerNotFound_propagatesException() {
        Mockito.when(rewardService.getRewardsForCustomer(999L, null, null))
                .thenThrow(new CustomerNotFoundException(999L));

        Assertions.assertThatThrownBy(() -> controller.getCustomerReward(999L, null, null))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getCustomerReward_invalidDateRange_propagatesException() {
        LocalDate from = LocalDate.of(2024, 3, 1);
        LocalDate to   = LocalDate.of(2024, 1, 1);
        Mockito.when(rewardService.getRewardsForCustomer(1L, from, to))
                .thenThrow(new InvalidDateRangeException(from, to));

        Assertions.assertThatThrownBy(() -> controller.getCustomerReward(1L, from, to))
                .isInstanceOf(InvalidDateRangeException.class);
    }
}
