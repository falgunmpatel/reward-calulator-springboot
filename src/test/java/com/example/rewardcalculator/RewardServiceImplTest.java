package com.example.rewardcalculator;

import com.example.rewardcalculator.exception.CustomerNotFoundException;
import com.example.rewardcalculator.model.Customer;
import com.example.rewardcalculator.model.Transaction;
import com.example.rewardcalculator.repository.CustomerRepository;
import com.example.rewardcalculator.repository.TransactionRepository;
import com.example.rewardcalculator.service.RewardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/**
 * Unit tests for {@link RewardServiceImpl}.
 *
 * <p>All repository dependencies are mocked via Mockito so no database or Spring context is needed.
 * Tests cover the points-calculation algorithm, monthly aggregation, totals, and all edge cases
 * including boundary values, cent truncation, missing customers, and customers with no transactions.</p>
 */
@ExtendWith(MockitoExtension.class)
class RewardServiceImplTest {

    @Mock
    CustomerRepository customerRepository;

    @Mock
    TransactionRepository transactionRepository;

    @InjectMocks
    RewardServiceImpl service;

    // -------------------------------------------------------------------------
    // Points calculation — boundary and edge-case tests
    // -------------------------------------------------------------------------

    /** $120.00 → (120-100)*2 + 50 = 90 points. */
    @Test
    void testPoints_above100() {
        assertThat(service.calculatePoints(new BigDecimal("120.00"))).isEqualTo(90);
    }

    /** $75.00 → (75-50)*1 = 25 points. */
    @Test
    void testPoints_between50and100() {
        assertThat(service.calculatePoints(new BigDecimal("75.00"))).isEqualTo(25);
    }

    /** $100.00 → exactly at upper boundary = 50 points. */
    @Test
    void testPoints_exactly100() {
        assertThat(service.calculatePoints(new BigDecimal("100.00"))).isEqualTo(50);
    }

    /** $50.00 → not over lower threshold = 0 points. */
    @Test
    void testPoints_exactly50() {
        assertThat(service.calculatePoints(new BigDecimal("50.00"))).isEqualTo(0);
    }

    /** $51.00 → first dollar over $50 = 1 point. */
    @Test
    void testPoints_exactly51() {
        assertThat(service.calculatePoints(new BigDecimal("51.00"))).isEqualTo(1);
    }

    /** $30.00 → below lower threshold = 0 points. */
    @Test
    void testPoints_below50() {
        assertThat(service.calculatePoints(new BigDecimal("30.00"))).isEqualTo(0);
    }

    /** $0.00 → 0 points. */
    @Test
    void testPoints_zero() {
        assertThat(service.calculatePoints(new BigDecimal("0.00"))).isEqualTo(0);
    }

    /** $200.00 → (200-100)*2 + 50 = 250 points. */
    @Test
    void testPoints_200() {
        assertThat(service.calculatePoints(new BigDecimal("200.00"))).isEqualTo(250);
    }

    /** $120.99 → cents truncated → treats as $120 → 90 points. */
    @Test
    void testPoints_centsIgnored_above100() {
        assertThat(service.calculatePoints(new BigDecimal("120.99"))).isEqualTo(90);
    }

    /** $100.99 → cents truncated → treats as $100 → 50 points. */
    @Test
    void testPoints_centsIgnored_at100() {
        assertThat(service.calculatePoints(new BigDecimal("100.99"))).isEqualTo(50);
    }

    /** $50.99 → cents truncated → treats as $50 → 0 points (not over $50). */
    @Test
    void testPoints_centsIgnored_at50() {
        assertThat(service.calculatePoints(new BigDecimal("50.99"))).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // Monthly aggregation and totals
    // -------------------------------------------------------------------------

    /**
     * Three transactions spanning two months are correctly aggregated per month.
     * Jan: $120 (90 pts) + $75 (25 pts) = 115; Feb: $200 = 250.
     */
    @Test
    void testMonthlyAggregation() {
        Customer c = customer(1L, "Alice");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(transactionRepository.findByCustomerId(1L)).thenReturn(List.of(
                tx(c, "120.00", "2024-01-15"),  // 90 pts
                tx(c, "75.00",  "2024-01-28"),  // 25 pts
                tx(c, "200.00", "2024-02-10")   // 250 pts
        ));

        var summary = service.getRewardsForCustomer(1L, null, null);

        assertThat(summary.monthlyRewards()).hasSize(2);
        assertThat(summary.monthlyRewards().get(0).points()).isEqualTo(115); // January
        assertThat(summary.monthlyRewards().get(1).points()).isEqualTo(250); // February
    }

    /**
     * Alice's 5 transactions across 3 months produce the expected total of 435 points.
     * Jan: 90+25=115, Feb: 250+0=250, Mar: 70 → total = 435.
     */
    @Test
    void testTotalPoints() {
        Customer c = customer(1L, "Alice");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(transactionRepository.findByCustomerId(1L)).thenReturn(List.of(
                tx(c, "120.00", "2024-01-15"),  // 90
                tx(c, "75.50",  "2024-01-28"),  // 25
                tx(c, "200.00", "2024-02-10"),  // 250
                tx(c, "45.00",  "2024-02-20"),  // 0
                tx(c, "110.00", "2024-03-05")   // 70
        ));

        assertThat(service.getRewardsForCustomer(1L, null, null).totalPoints()).isEqualTo(435);
    }

    // -------------------------------------------------------------------------
    // getRewardsForAllCustomers
    // -------------------------------------------------------------------------

    /**
     * Verifies that all three customers are included in the result list with correct IDs.
     */
    @Test
    void testGetAllRewards_allCustomersPresent() {
        Customer alice = customer(1L, "Alice Johnson");
        Customer bob   = customer(2L, "Bob Smith");
        Customer carol = customer(3L, "Carol White");

        when(customerRepository.findAll()).thenReturn(List.of(alice, bob, carol));
        when(transactionRepository.findByCustomerId(1L)).thenReturn(List.of());
        when(transactionRepository.findByCustomerId(2L)).thenReturn(List.of());
        when(transactionRepository.findByCustomerId(3L)).thenReturn(List.of());

        var results = service.getRewardsForAllCustomers();

        assertThat(results).hasSize(3);
        assertThat(results).extracting(s -> s.customerName())
                .containsExactly("Alice Johnson", "Bob Smith", "Carol White");
    }

    // -------------------------------------------------------------------------
    // Exception / negative scenarios
    // -------------------------------------------------------------------------

    /**
     * Requesting a non-existent customer ID must throw {@link CustomerNotFoundException}
     * with a message containing the requested ID.
     */
    @Test
    void testCustomerNotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getRewardsForCustomer(999L, null, null))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("999");
    }

    /**
     * A customer with no transactions must return totalPoints = 0
     * and an empty (not null) monthlyRewards list.
     */
    @Test
    void testCustomerNoTransactions() {
        Customer c = customer(1L, "Alice");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(transactionRepository.findByCustomerId(1L)).thenReturn(List.of());

        var summary = service.getRewardsForCustomer(1L, null, null);

        assertThat(summary.totalPoints()).isZero();
        assertThat(summary.monthlyRewards()).isNotNull().isEmpty();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Customer customer(Long id, String name) {
        Customer c = new Customer();
        c.setId(id);
        c.setName(name);
        return c;
    }

    private Transaction tx(Customer c, String amount, String date) {
        Transaction t = new Transaction();
        t.setCustomer(c);
        t.setAmount(new BigDecimal(amount));
        t.setTransactionDate(LocalDate.parse(date));
        return t;
    }
}
