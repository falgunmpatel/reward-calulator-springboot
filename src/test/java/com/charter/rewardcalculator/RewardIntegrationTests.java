package com.charter.rewardcalculator;

import com.charter.rewardcalculator.controller.RewardController;
import com.charter.rewardcalculator.dto.CustomerRewardSummaryDTO;
import com.charter.rewardcalculator.dto.MonthlyRewardDTO;
import com.charter.rewardcalculator.dto.PagedRewardSummaryDTO;
import com.charter.rewardcalculator.exception.CustomerNotFoundException;
import com.charter.rewardcalculator.exception.InvalidDateRangeException;
import com.charter.rewardcalculator.service.RewardService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.List;

@WebMvcTest(RewardController.class)
class RewardIntegrationTests {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RewardService rewardService;

    // --- GET /api/rewards ---

    @Test
    void getAllRewards_returnsOkWithPagedContent() throws Exception {
        var summary = new CustomerRewardSummaryDTO(1L, "Alice Johnson",
                List.of(new MonthlyRewardDTO(2024, "JANUARY", 115)), 115L);
        var paged = new PagedRewardSummaryDTO(List.of(summary), 0, 10, 1, 1, true);
        Mockito.when(rewardService.getRewardsPaged(ArgumentMatchers.any(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(paged);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].customerId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].customerName").value("Alice Johnson"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.page").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.last").value(true));
    }

    @Test
    void getAllRewards_callsServiceWithCorrectPageable() throws Exception {
        var paged = new PagedRewardSummaryDTO(List.of(), 0, 10, 0, 0, true);
        Mockito.when(rewardService.getRewardsPaged(ArgumentMatchers.any(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(paged);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards?page=0&size=10").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(rewardService).getRewardsPaged(
                ArgumentMatchers.eq(PageRequest.of(0, 10, Sort.by("id").ascending())), ArgumentMatchers.isNull(), ArgumentMatchers.isNull());
    }

    @Test
    void getAllRewards_withDateFilter_passesFromAndToToService() throws Exception {
        var paged = new PagedRewardSummaryDTO(List.of(), 0, 10, 0, 0, true);
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to   = LocalDate.of(2024, 1, 31);
        Mockito.when(rewardService.getRewardsPaged(ArgumentMatchers.any(), ArgumentMatchers.eq(from), ArgumentMatchers.eq(to))).thenReturn(paged);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards?from=2024-01-01&to=2024-01-31")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(rewardService).getRewardsPaged(ArgumentMatchers.any(), ArgumentMatchers.eq(from), ArgumentMatchers.eq(to));
    }

    @Test
    void getAllRewards_withOnlyFrom_passesToService() throws Exception {
        var paged = new PagedRewardSummaryDTO(List.of(), 0, 10, 0, 0, true);
        LocalDate from = LocalDate.of(2024, 2, 1);
        Mockito.when(rewardService.getRewardsPaged(ArgumentMatchers.any(), ArgumentMatchers.eq(from), ArgumentMatchers.isNull())).thenReturn(paged);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards?from=2024-02-01").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(rewardService).getRewardsPaged(ArgumentMatchers.any(), ArgumentMatchers.eq(from), ArgumentMatchers.isNull());
    }

    @Test
    void getAllRewards_withOnlyTo_passesToService() throws Exception {
        var paged = new PagedRewardSummaryDTO(List.of(), 0, 10, 0, 0, true);
        LocalDate to = LocalDate.of(2024, 1, 31);
        Mockito.when(rewardService.getRewardsPaged(ArgumentMatchers.any(), ArgumentMatchers.isNull(), ArgumentMatchers.eq(to))).thenReturn(paged);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards?to=2024-01-31").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(rewardService).getRewardsPaged(ArgumentMatchers.any(), ArgumentMatchers.isNull(), ArgumentMatchers.eq(to));
    }

    @Test
    void getAllRewards_responseContentTypeIsJson() throws Exception {
        var paged = new PagedRewardSummaryDTO(List.of(), 0, 10, 0, 0, true);
        Mockito.when(rewardService.getRewardsPaged(ArgumentMatchers.any(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(paged);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getAllRewards_emptyContent_returnsEmptyArray() throws Exception {
        var paged = new PagedRewardSummaryDTO(List.of(), 0, 10, 0, 0, true);
        Mockito.when(rewardService.getRewardsPaged(ArgumentMatchers.any(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(paged);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(0));
    }

    @Test
    void getAllRewards_pageNegative_returns400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards?page=-1").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").exists());
    }

    @Test
    void getAllRewards_sizeZero_returns400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards?size=0").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400));
    }

    @Test
    void getAllRewards_invalidFromDate_returns400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards?from=not-a-date").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400));
    }

    @Test
    void getAllRewards_serviceThrowsInvalidDateRange_returns400() throws Exception {
        Mockito.when(rewardService.getRewardsPaged(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenThrow(new InvalidDateRangeException(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 1, 1)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards?from=2024-03-01&to=2024-01-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400));
    }

    @Test
    void getAllRewards_serviceThrowsUnexpected_returns500() throws Exception {
        Mockito.when(rewardService.getRewardsPaged(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("db down"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(500))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Internal Server Error"));
    }

    // --- GET /api/rewards/{customerId} ---

    @Test
    void getCustomerReward_returnsOkWithSummary() throws Exception {
        var summary = new CustomerRewardSummaryDTO(1L, "Alice Johnson",
                List.of(new MonthlyRewardDTO(2024, "JANUARY", 115)), 115L);
        Mockito.when(rewardService.getRewardsForCustomer(ArgumentMatchers.eq(1L), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(summary);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerName").value("Alice Johnson"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPoints").value(115))
                .andExpect(MockMvcResultMatchers.jsonPath("$.monthlyRewards[0].month").value("JANUARY"));
    }

    @Test
    void getCustomerReward_callsServiceWithCorrectArgs() throws Exception {
        var summary = new CustomerRewardSummaryDTO(1L, "Alice", List.of(), 0L);
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to   = LocalDate.of(2024, 1, 31);
        Mockito.when(rewardService.getRewardsForCustomer(ArgumentMatchers.eq(1L), ArgumentMatchers.eq(from), ArgumentMatchers.eq(to))).thenReturn(summary);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards/1?from=2024-01-01&to=2024-01-31")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(rewardService).getRewardsForCustomer(1L, from, to);
    }

    @Test
    void getCustomerReward_withOnlyFrom_passesToService() throws Exception {
        var summary = new CustomerRewardSummaryDTO(1L, "Alice", List.of(), 0L);
        LocalDate from = LocalDate.of(2024, 2, 1);
        Mockito.when(rewardService.getRewardsForCustomer(ArgumentMatchers.eq(1L), ArgumentMatchers.eq(from), ArgumentMatchers.isNull())).thenReturn(summary);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards/1?from=2024-02-01").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(rewardService).getRewardsForCustomer(1L, from, null);
    }

    @Test
    void getCustomerReward_withOnlyTo_passesToService() throws Exception {
        var summary = new CustomerRewardSummaryDTO(1L, "Alice", List.of(), 0L);
        LocalDate to = LocalDate.of(2024, 1, 31);
        Mockito.when(rewardService.getRewardsForCustomer(ArgumentMatchers.eq(1L), ArgumentMatchers.isNull(), ArgumentMatchers.eq(to))).thenReturn(summary);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards/1?to=2024-01-31").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(rewardService).getRewardsForCustomer(1L, null, to);
    }

    @Test
    void getCustomerReward_fromEqualsTo_passedToService() throws Exception {
        LocalDate day = LocalDate.of(2024, 1, 15);
        var summary = new CustomerRewardSummaryDTO(1L, "Alice", List.of(new MonthlyRewardDTO(2024, "JANUARY", 90)), 90L);
        Mockito.when(rewardService.getRewardsForCustomer(ArgumentMatchers.eq(1L), ArgumentMatchers.eq(day), ArgumentMatchers.eq(day))).thenReturn(summary);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards/1?from=2024-01-15&to=2024-01-15").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPoints").value(90));

        Mockito.verify(rewardService).getRewardsForCustomer(1L, day, day);
    }

    @Test
    void getCustomerReward_noTransactions_returnsEmptyMonthlyRewards() throws Exception {
        var summary = new CustomerRewardSummaryDTO(1L, "Alice", List.of(), 0L);
        Mockito.when(rewardService.getRewardsForCustomer(ArgumentMatchers.eq(1L), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(summary);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.monthlyRewards").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPoints").value(0));
    }

    @Test
    void getCustomerReward_customerNotFound_returns404() throws Exception {
        Mockito.when(rewardService.getRewardsForCustomer(ArgumentMatchers.eq(999L), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenThrow(new CustomerNotFoundException(999L));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards/999").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Not Found"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(Matchers.containsString("999")));
    }

    @Test
    void getCustomerReward_invalidDateRange_returns400() throws Exception {
        Mockito.when(rewardService.getRewardsForCustomer(ArgumentMatchers.eq(1L), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenThrow(new InvalidDateRangeException(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 1, 1)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards/1?from=2024-03-01&to=2024-01-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400));
    }

    @Test
    void getCustomerReward_idZero_returns400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards/0").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400));
    }

    @Test
    void getCustomerReward_negativeId_returns400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards/-1").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400));
    }

    @Test
    void getCustomerReward_nonNumericId_returns400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/rewards/abc").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400));
    }
}
