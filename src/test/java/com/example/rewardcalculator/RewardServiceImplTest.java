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
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RewardServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class RewardServiceImplTest {

    @Mock CustomerRepository customerRepository;
    @Mock TransactionRepository transactionRepository;
    @InjectMocks RewardServiceImpl service;

    @Test void testPoints_above100() {
        assertThat(service.calculatePoints(new BigDecimal("120.00"))).isEqualTo(90);
    }

    @Test void testPoints_between50and100() {
        assertThat(service.calculatePoints(new BigDecimal("75.00"))).isEqualTo(25);
    }

    @Test void testPoints_exactly100() {
        assertThat(service.calculatePoints(new BigDecimal("100.00"))).isEqualTo(50);
    }

    @Test void testPoints_exactly50() {
        assertThat(service.calculatePoints(new BigDecimal("50.00"))).isEqualTo(0);
    }

    @Test void testPoints_below50() {
        assertThat(service.calculatePoints(new BigDecimal("30.00"))).isEqualTo(0);
    }

    @Test void testPoints_zero() {
        assertThat(service.calculatePoints(new BigDecimal("0.00"))).isEqualTo(0);
    }

    @Test void testPoints_centsIgnored() {
        assertThat(service.calculatePoints(new BigDecimal("120.99"))).isEqualTo(90);
    }

    @Test void testPoints_200() {
        assertThat(service.calculatePoints(new BigDecimal("200.00"))).isEqualTo(250);
    }

    @Test
    void testMonthlyAggregation() {
        Customer c = customer(1L, "Alice");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(transactionRepository.findByCustomerId(1L)).thenReturn(List.of(
                tx(c, "120.00", "2024-01-15"),  // 90 pts
                tx(c, "75.00",  "2024-01-28"),  // 25 pts
                tx(c, "200.00", "2024-02-10")   // 250 pts
        ));

        var summary = service.getRewardsForCustomer(1L);

        assertThat(summary.getMonthlyRewards()).hasSize(2);
        assertThat(summary.getMonthlyRewards().get(0).getPoints()).isEqualTo(115); // Jan
        assertThat(summary.getMonthlyRewards().get(1).getPoints()).isEqualTo(250); // Feb
    }

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

        assertThat(service.getRewardsForCustomer(1L).getTotalPoints()).isEqualTo(435);
    }

    @Test
    void testCustomerNotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getRewardsForCustomer(999L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void testCustomerNoTransactions() {
        Customer c = customer(1L, "Alice");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(transactionRepository.findByCustomerId(1L)).thenReturn(List.of());

        var summary = service.getRewardsForCustomer(1L);

        assertThat(summary.getTotalPoints()).isZero();
        assertThat(summary.getMonthlyRewards()).isEmpty();
    }

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
