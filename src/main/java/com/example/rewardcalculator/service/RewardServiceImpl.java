package com.example.rewardcalculator.service;

import com.example.rewardcalculator.dto.CustomerRewardSummaryDTO;
import com.example.rewardcalculator.dto.MonthlyRewardDTO;
import com.example.rewardcalculator.exception.CustomerNotFoundException;
import com.example.rewardcalculator.model.Customer;
import com.example.rewardcalculator.model.Transaction;
import com.example.rewardcalculator.repository.CustomerRepository;
import com.example.rewardcalculator.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation of {@link RewardService} containing all reward-point business logic.
 *
 * <p>All public methods are read-only transactions; no data is written by this service.</p>
 */
@Service
public class RewardServiceImpl implements RewardService {

    private static final long LOWER_THRESHOLD = 50L;
    private static final long UPPER_THRESHOLD = 100L;

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Constructs the service with the required repositories.
     *
     * @param customerRepository    repository for customer data
     * @param transactionRepository repository for transaction data
     */
    public RewardServiceImpl(CustomerRepository customerRepository,
                             TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<CustomerRewardSummaryDTO> getRewardsForAllCustomers() {
        return customerRepository.findAll().stream()
                .map(c -> buildSummary(c, transactionRepository.findByCustomerId(c.getId())))
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public CustomerRewardSummaryDTO getRewardsForCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        return buildSummary(customer, transactionRepository.findByCustomerId(customerId));
    }

    /**
     * Builds a {@link CustomerRewardSummaryDTO} from a customer and their transactions.
     * Months are derived dynamically from transaction dates — never hardcoded.
     *
     * @param customer     the customer entity
     * @param transactions the customer's transactions (may be empty)
     * @return fully populated reward summary; {@code monthlyRewards} is empty (not null) when
     *         the customer has no transactions
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
     * Cents are truncated via {@link BigDecimal#longValue()}; only the integer dollar portion is used.
     *
     * <ul>
     *   <li>$0–$50: 0 points</li>
     *   <li>$50.01–$100: 1 point per dollar over $50</li>
     *   <li>Over $100: 50 points (the $50–$100 band) + 2 points per dollar over $100</li>
     * </ul>
     *
     * @param amount the transaction amount; must be non-negative
     * @return reward points earned (always &ge; 0)
     */
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
