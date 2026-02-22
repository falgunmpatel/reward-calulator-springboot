package com.example.rewardcalculator.service;

import com.example.rewardcalculator.dto.CustomerRewardSummaryDTO;
import com.example.rewardcalculator.dto.MonthlyRewardDTO;
import com.example.rewardcalculator.dto.PagedRewardSummaryDTO;
import com.example.rewardcalculator.exception.CustomerNotFoundException;
import com.example.rewardcalculator.exception.InvalidDateRangeException;
import com.example.rewardcalculator.model.Customer;
import com.example.rewardcalculator.model.Transaction;
import com.example.rewardcalculator.repository.CustomerRepository;
import com.example.rewardcalculator.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class RewardServiceImpl implements RewardService {

    private static final Logger log = LoggerFactory.getLogger(RewardServiceImpl.class);

    private static final long LOWER_THRESHOLD = 50L;  // $50  → 1 pt/dollar above this
    private static final long UPPER_THRESHOLD = 100L; // $100 → 2 pts/dollar above this

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public RewardServiceImpl(CustomerRepository customerRepository,
                             TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerRewardSummaryDTO> getRewardsForAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        log.info("Calculating rewards for all customers — total customers found: {}", customers.size());

        List<CustomerRewardSummaryDTO> summaries = customers.stream()
                .map(c -> buildSummary(c, transactionRepository.findByCustomerId(c.getId())))
                .toList();

        log.debug("Reward calculation complete for all {} customers", summaries.size());
        return summaries;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedRewardSummaryDTO getRewardsPaged(Pageable pageable, LocalDate from, LocalDate to) {
        Page<Customer> customerPage = customerRepository.findAll(pageable);
        log.info("Fetching paged rewards — page={}, size={}, from={}, to={}",
                pageable.getPageNumber(), pageable.getPageSize(), from, to);

        List<CustomerRewardSummaryDTO> content = customerPage.getContent().stream()
                .map(c -> buildSummary(c, fetchTransactions(c.getId(), from, to)))
                .toList();

        log.debug("Paged rewards built — page {}/{}, items={}",
                customerPage.getNumber() + 1, customerPage.getTotalPages(), content.size());

        return new PagedRewardSummaryDTO(
                content,
                customerPage.getNumber(),
                customerPage.getSize(),
                customerPage.getTotalElements(),
                customerPage.getTotalPages(),
                customerPage.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerRewardSummaryDTO getRewardsForCustomer(Long customerId, LocalDate from, LocalDate to) {
        log.info("Fetching reward summary for customerId={}, from={}, to={}", customerId, from, to);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.warn("Customer not found for customerId={}", customerId);
                    return new CustomerNotFoundException(customerId);
                });

        List<Transaction> transactions = fetchTransactions(customerId, from, to);
        log.debug("Found {} transaction(s) for customerId={}", transactions.size(), customerId);

        CustomerRewardSummaryDTO summary = buildSummary(customer, transactions);
        log.info("Reward summary for customerId={}: totalPoints={}", customerId, summary.totalPoints());
        return summary;
    }

    // Returns all transactions for a customer, or a date-filtered subset if from/to are provided.
    // Null bounds default to LocalDate.MIN / LocalDate.MAX respectively.
    private List<Transaction> fetchTransactions(Long customerId, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return transactionRepository.findByCustomerId(customerId);
        }
        LocalDate effectiveFrom = (from != null) ? from : LocalDate.MIN;
        LocalDate effectiveTo   = (to   != null) ? to   : LocalDate.MAX;
        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new InvalidDateRangeException(effectiveFrom, effectiveTo);
        }
        return transactionRepository
                .findByCustomerIdAndTransactionDateBetween(customerId, effectiveFrom, effectiveTo);
    }

    // Aggregates per-month reward points from transactions. Uses TreeMap so months come out sorted.
    private CustomerRewardSummaryDTO buildSummary(Customer customer, List<Transaction> transactions) {
        log.debug("Building reward summary for customerId={}, name='{}', transactions={}",
                customer.getId(), customer.getName(), transactions.size());

        Map<YearMonth, Long> monthlyMap = new TreeMap<>();

        for (Transaction tx : transactions) {
            YearMonth ym = YearMonth.from(tx.getTransactionDate());
            long pts = calculatePoints(tx.getAmount());
            monthlyMap.merge(ym, pts, Long::sum);
            log.trace("transactionId={} | date={} | amount={} | points={}", tx.getId(), tx.getTransactionDate(), tx.getAmount(), pts);
        }

        List<MonthlyRewardDTO> monthlyRewards = monthlyMap.entrySet().stream()
                .map(e -> new MonthlyRewardDTO(
                        e.getKey().getYear(),
                        e.getKey().getMonth().name(),
                        e.getValue()))
                .toList();

        long totalPoints = monthlyMap.values().stream().mapToLong(Long::longValue).sum();

        log.debug("customerId={} | monthsWithActivity={} | totalPoints={}", customer.getId(), monthlyRewards.size(), totalPoints);
        return new CustomerRewardSummaryDTO(customer.getId(), customer.getName(), monthlyRewards, totalPoints);
    }

    // $0–$50 → 0 pts | $50–$100 → 1 pt/dollar over $50 | >$100 → 50 pts + 2 pts/dollar over $100
    @Override
    public long calculatePoints(BigDecimal amount) {
        long dollars = amount.longValue();
        long points = 0;

        if (dollars > UPPER_THRESHOLD) {
            points += (dollars - UPPER_THRESHOLD) * 2;  // 2 pts/dollar above $100
            points += UPPER_THRESHOLD - LOWER_THRESHOLD; // flat 50 pts for the $50–$100 band
        } else if (dollars > LOWER_THRESHOLD) {
            points += dollars - LOWER_THRESHOLD;         // 1 pt/dollar above $50
        }

        log.trace("calculatePoints: amount=${} → dollars={} → points={}", amount, dollars, points);
        return points;
    }
}
