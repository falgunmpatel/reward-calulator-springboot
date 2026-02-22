package com.example.rewardcalculator.controller;

import com.example.rewardcalculator.dto.CustomerRewardSummaryDTO;
import com.example.rewardcalculator.dto.PagedRewardSummaryDTO;
import com.example.rewardcalculator.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/rewards")
@Validated
@Tag(name = "Rewards", description = "Customer reward points calculation API")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @Operation(summary = "Get paginated reward summaries for all customers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(schema = @Schema(implementation = PagedRewardSummaryDTO.class)))
    })
    @GetMapping
    public ResponseEntity<PagedRewardSummaryDTO> getAllRewards(
            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) int size,

            @Parameter(description = "Start date filter (inclusive, ISO-8601)", example = "2024-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "End date filter (inclusive, ISO-8601)", example = "2024-03-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        PagedRewardSummaryDTO result = rewardService.getRewardsPaged(pageable, from, to);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get reward summary for a specific customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(schema = @Schema(implementation = CustomerRewardSummaryDTO.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerRewardSummaryDTO> getCustomerReward(
            @Parameter(description = "Customer ID (must be >= 1)", example = "1", required = true)
            @PathVariable @Min(1) Long customerId,

            @Parameter(description = "Start date filter (inclusive, ISO-8601)", example = "2024-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "End date filter (inclusive, ISO-8601)", example = "2024-03-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        CustomerRewardSummaryDTO summary = rewardService.getRewardsForCustomer(customerId, from, to);
        return ResponseEntity.ok(summary);
    }
}
