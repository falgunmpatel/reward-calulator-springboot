package com.example.rewardcalculator.service;

import com.example.rewardcalculator.dto.CustomerRewardSummaryDTO;
import com.example.rewardcalculator.dto.MonthlyRewardDTO;
import com.example.rewardcalculator.exception.CustomerNotFoundException;
import com.example.rewardcalculator.model.Customer;
import com.example.rewardcalculator.model.Transaction;
import com.example.rewardcalculator.repository.CustomerRepository;
import com.example.rewardcalculator.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation of {@link RewardService} containing all reward-point business logic.
 */
@Service
public class RewardServiceImpl implements RewardService {

    private static final long LOWER_THRESHOLD = 50L;
    private static final long UPPER_THRESHOLD = 100L;

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public RewardServiceImpl(CustomerRepository customerRepository,
                             TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    /** {@inheritDoc} */
    @Override
    public List<CustomerRewardSummaryDTO> getRewardsForAllCustomers() {
        return customerRepository.findAll().stream()
                .map(c -> buildSummary(c, transactionRepository.findByCustomerId(c.getId())))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public CustomerRewardSummaryDTO getRewardsForCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        return buildSummary(customer, transactionRepository.findByCustomerId(customerId));
    }

    /**
     * Builds a {@link CustomerRewardSummaryDTO} from a customer and their transactions.
     *
     * @param customer     the customer entity
     * @param transactions the customer's transactions
     * @return fully populated reward summary
     */
    private CustomerRewardSummaryDTO buildSummary(Customer customer, List<Transaction> transactions) {
        Map<YearMonth, Long> monthlyMap = new TreeMap<>();

        for (Transaction tx : transactions) {
            YearMonth ym = YearMonth.from(tx.getTransactionDate());
            monthlyMap.merge(ym, calculatePoints(tx.getAmount()), Long::sum);
        }

        List<MonthlyRewardDTO> monthlyRewards = monthlyMap.entrySet().stream()
                .map(e -> new MonthlyRewardDTO(
                        e.getKey().getYear(),
                        e.getKey().getMonth().name(),
                        e.getValue()))
                .toList();

        long totalPoints = monthlyMap.values().stream().mapToLong(Long::longValue).sum();

        return new CustomerRewardSummaryDTO(customer.getId(), customer.getName(), monthlyRewards, totalPoints);
    }

    /**
     * Calculates reward points for a single transaction amount.
     * Cents are truncated; only the integer dollar portion is used.
     *
     * <ul>
     *   <li>$0–$50: 0 points</li>
     *   <li>$50.01–$100: 1 point per dollar over $50</li>
     *   <li>Over $100: 50 points (the $50–$100 band) + 2 points per dollar over $100</li>
     * </ul>
     *
     * @param amount the transaction amount
     * @return reward points earned
     */
    long calculatePoints(BigDecimal amount) {
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
