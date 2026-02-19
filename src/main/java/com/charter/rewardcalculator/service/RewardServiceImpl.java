package com.charter.rewardcalculator.service;

import com.charter.rewardcalculator.dto.CustomerRewardSummaryDTO;
import com.charter.rewardcalculator.dto.MonthlyRewardDTO;
import com.charter.rewardcalculator.dto.PagedRewardSummaryDTO;
import com.charter.rewardcalculator.exception.CustomerNotFoundException;
import com.charter.rewardcalculator.exception.InvalidDateRangeException;
import com.charter.rewardcalculator.model.Customer;
import com.charter.rewardcalculator.model.Transaction;
import com.charter.rewardcalculator.repository.CustomerRepository;
import com.charter.rewardcalculator.repository.TransactionRepository;
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

/** Implementation of RewardService that computes reward summaries from DB data. */
@Service
public class RewardServiceImpl implements RewardService {

    private static final Logger log = LoggerFactory.getLogger(RewardServiceImpl.class);

    private static final long LOWER_THRESHOLD = 50L;
    private static final long UPPER_THRESHOLD = 100L;

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    /** Constructs the service with customer and transaction repositories. */
    public RewardServiceImpl(CustomerRepository customerRepository,
                             TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PagedRewardSummaryDTO getRewardsPaged(Pageable pageable, LocalDate from, LocalDate to) {
        Page<Customer> customerPage = customerRepository.findAll(pageable);
        log.info("Fetching paged rewards — page={}, size={}, from={}, to={}",
                pageable.getPageNumber(), pageable.getPageSize(), from, to);

        List<CustomerRewardSummaryDTO> content = customerPage.getContent().stream()
                .map(c -> buildSummary(c, fetchTransactions(c.getId(), from, to)))
                .toList();

        return new PagedRewardSummaryDTO(
                content,
                customerPage.getNumber(),
                customerPage.getSize(),
                customerPage.getTotalElements(),
                customerPage.getTotalPages(),
                customerPage.isLast());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public CustomerRewardSummaryDTO getRewardsForCustomer(Long customerId, LocalDate from, LocalDate to) {
        log.info("Fetching reward summary for customerId={}, from={}, to={}", customerId, from, to);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        return buildSummary(customer, fetchTransactions(customerId, from, to));
    }

    /** Fetches transactions for a customer applying whichever date bounds are provided. */
    private List<Transaction> fetchTransactions(Long customerId, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return transactionRepository.findByCustomerId(customerId);
        }
        if (from != null && to != null) {
            if (from.isAfter(to)) {
                throw new InvalidDateRangeException(from, to);
            }
            return transactionRepository.findByCustomerIdAndTransactionDateBetween(customerId, from, to);
        }
        if (from != null) {
            return transactionRepository.findByCustomerIdAndTransactionDateGreaterThanEqual(customerId, from);
        }
        return transactionRepository.findByCustomerIdAndTransactionDateLessThanEqual(customerId, to);
    }

    /** Aggregates transaction points by month and builds the customer reward summary. */
    private CustomerRewardSummaryDTO buildSummary(Customer customer, List<Transaction> transactions) {
        Map<YearMonth, Long> monthlyMap = new TreeMap<>();
        for (Transaction tx : transactions) {
            YearMonth ym = YearMonth.from(tx.getTransactionDate());
            monthlyMap.merge(ym, calculatePoints(tx.getAmount()), Long::sum);
        }
        List<MonthlyRewardDTO> monthlyRewards = monthlyMap.entrySet().stream()
                .map(e -> new MonthlyRewardDTO(e.getKey().getYear(), e.getKey().getMonth().name(), e.getValue()))
                .toList();
        long totalPoints = monthlyMap.values().stream().mapToLong(Long::longValue).sum();
        return new CustomerRewardSummaryDTO(customer.getId(), customer.getName(), monthlyRewards, totalPoints);
    }

    /** {@inheritDoc} */
    @Override
    public long calculatePoints(BigDecimal amount) {
        long dollars = amount.longValue();
        long points = 0;
        if (dollars > UPPER_THRESHOLD) {
            points += (dollars - UPPER_THRESHOLD) * 2;
            points += UPPER_THRESHOLD - LOWER_THRESHOLD;
        } else if (dollars > LOWER_THRESHOLD) {
            points += dollars - LOWER_THRESHOLD;
        }
        return points;
    }
}
