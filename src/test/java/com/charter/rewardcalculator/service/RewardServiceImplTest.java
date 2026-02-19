package com.charter.rewardcalculator.service;

import com.charter.rewardcalculator.exception.CustomerNotFoundException;
import com.charter.rewardcalculator.exception.InvalidDateRangeException;
import com.charter.rewardcalculator.model.Customer;
import com.charter.rewardcalculator.model.Transaction;
import com.charter.rewardcalculator.repository.CustomerRepository;
import com.charter.rewardcalculator.repository.TransactionRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class RewardServiceImplTest {

    @Mock
    CustomerRepository customerRepository;

    @Mock
    TransactionRepository transactionRepository;

    @InjectMocks
    RewardServiceImpl service;

    @Test
    void calculatePoints_zero() {
        Assertions.assertThat(service.calculatePoints(new BigDecimal("0.00"))).isEqualTo(0);
    }

    @Test
    void calculatePoints_below50() {
        Assertions.assertThat(service.calculatePoints(new BigDecimal("30.00"))).isEqualTo(0);
    }

    @Test
    void calculatePoints_49_99_truncatedTo49() {
        Assertions.assertThat(service.calculatePoints(new BigDecimal("49.99"))).isEqualTo(0);
    }

    @Test
    void calculatePoints_exactly50() {
        Assertions.assertThat(service.calculatePoints(new BigDecimal("50.00"))).isEqualTo(0);
    }

    @Test
    void calculatePoints_50_99_centsIgnored() {
        Assertions.assertThat(service.calculatePoints(new BigDecimal("50.99"))).isEqualTo(0);
    }

    @Test
    void calculatePoints_exactly51_firstDollarIn1xBand() {
        Assertions.assertThat(service.calculatePoints(new BigDecimal("51.00"))).isEqualTo(1);
    }

    @Test
    void calculatePoints_75() {
        Assertions.assertThat(service.calculatePoints(new BigDecimal("75.00"))).isEqualTo(25);
    }

    @Test
    void calculatePoints_exactly100() {
        Assertions.assertThat(service.calculatePoints(new BigDecimal("100.00"))).isEqualTo(50);
    }

    @Test
    void calculatePoints_100_99_centsIgnored() {
        Assertions.assertThat(service.calculatePoints(new BigDecimal("100.99"))).isEqualTo(50);
    }

    @Test
    void calculatePoints_101_firstDollarIn2xBand() {
        Assertions.assertThat(service.calculatePoints(new BigDecimal("101.00"))).isEqualTo(52);
    }

    @Test
    void calculatePoints_120() {
        Assertions.assertThat(service.calculatePoints(new BigDecimal("120.00"))).isEqualTo(90);
    }

    @Test
    void calculatePoints_120_99_centsIgnored() {
        Assertions.assertThat(service.calculatePoints(new BigDecimal("120.99"))).isEqualTo(90);
    }

    @Test
    void calculatePoints_200() {
        Assertions.assertThat(service.calculatePoints(new BigDecimal("200.00"))).isEqualTo(250);
    }

    @Test
    void calculatePoints_negative_returnsZero() {
        Assertions.assertThat(service.calculatePoints(new BigDecimal("-10.00"))).isEqualTo(0);
    }

    @Test
    void getRewardsForCustomer_notFound_throwsCustomerNotFoundException() {
        Mockito.when(customerRepository.findById(999L)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> service.getRewardsForCustomer(999L, null, null))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getRewardsForCustomer_noTransactions_returnsZeroPoints() {
        Customer c = customer(1L, "Alice");
        Mockito.when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        Mockito.when(transactionRepository.findByCustomerId(1L)).thenReturn(List.of());

        var summary = service.getRewardsForCustomer(1L, null, null);

        Assertions.assertThat(summary.totalPoints()).isZero();
        Assertions.assertThat(summary.monthlyRewards()).isEmpty();
    }

    @Test
    void getRewardsForCustomer_monthlyAggregationAndOrder() {
        Customer c = customer(1L, "Alice");
        Mockito.when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        Mockito.when(transactionRepository.findByCustomerId(1L)).thenReturn(List.of(
                tx(c, "120.00", "2024-01-15"),  // 90 pts
                tx(c, "75.00",  "2024-01-28"),  // 25 pts
                tx(c, "200.00", "2024-02-10")   // 250 pts
        ));

        var summary = service.getRewardsForCustomer(1L, null, null);

        Assertions.assertThat(summary.monthlyRewards()).hasSize(2);
        Assertions.assertThat(summary.monthlyRewards().get(0).month()).isEqualTo("JANUARY");
        Assertions.assertThat(summary.monthlyRewards().get(0).points()).isEqualTo(115);
        Assertions.assertThat(summary.monthlyRewards().get(1).month()).isEqualTo("FEBRUARY");
        Assertions.assertThat(summary.monthlyRewards().get(1).points()).isEqualTo(250);
    }

    @Test
    void getRewardsForCustomer_totalPoints() {
        Customer c = customer(1L, "Alice");
        Mockito.when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        Mockito.when(transactionRepository.findByCustomerId(1L)).thenReturn(List.of(
                tx(c, "120.00", "2024-01-15"),  // 90
                tx(c, "75.50",  "2024-01-28"),  // 25
                tx(c, "200.00", "2024-02-10"),  // 250
                tx(c, "45.00",  "2024-02-20"),  // 0
                tx(c, "110.00", "2024-03-05")   // 70
        ));

        Assertions.assertThat(service.getRewardsForCustomer(1L, null, null).totalPoints()).isEqualTo(435);
    }

    @Test
    void getRewardsForCustomer_withFromAndTo_callsDateRangeRepo() {
        Customer c = customer(1L, "Alice");
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to   = LocalDate.of(2024, 1, 31);
        Mockito.when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        Mockito.when(transactionRepository.findByCustomerIdAndTransactionDateBetween(1L, from, to))
                .thenReturn(List.of(tx(c, "120.00", "2024-01-15")));

        var summary = service.getRewardsForCustomer(1L, from, to);

        Assertions.assertThat(summary.totalPoints()).isEqualTo(90);
        Mockito.verify(transactionRepository).findByCustomerIdAndTransactionDateBetween(1L, from, to);
    }

    @Test
    void getRewardsForCustomer_withOnlyFrom_usesGreaterThanEqualQuery() {
        Customer c = customer(1L, "Alice");
        LocalDate from = LocalDate.of(2024, 2, 1);
        Mockito.when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        Mockito.when(transactionRepository.findByCustomerIdAndTransactionDateGreaterThanEqual(1L, from))
                .thenReturn(List.of());

        service.getRewardsForCustomer(1L, from, null);

        Mockito.verify(transactionRepository).findByCustomerIdAndTransactionDateGreaterThanEqual(1L, from);
    }

    @Test
    void getRewardsForCustomer_withOnlyTo_usesLessThanEqualQuery() {
        Customer c = customer(1L, "Alice");
        LocalDate to = LocalDate.of(2024, 1, 31);
        Mockito.when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        Mockito.when(transactionRepository.findByCustomerIdAndTransactionDateLessThanEqual(1L, to))
                .thenReturn(List.of());

        service.getRewardsForCustomer(1L, null, to);

        Mockito.verify(transactionRepository).findByCustomerIdAndTransactionDateLessThanEqual(1L, to);
    }

    @Test
    void getRewardsForCustomer_fromAfterTo_throwsInvalidDateRangeException() {
        Customer c = customer(1L, "Alice");
        Mockito.when(customerRepository.findById(1L)).thenReturn(Optional.of(c));

        Assertions.assertThatThrownBy(() -> service.getRewardsForCustomer(
                1L, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 1, 1)))
                .isInstanceOf(InvalidDateRangeException.class);
    }

    @Test
    void getRewardsForCustomer_fromEqualsTo_singleDayRange() {
        Customer c = customer(1L, "Alice");
        LocalDate day = LocalDate.of(2024, 1, 15);
        Mockito.when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        Mockito.when(transactionRepository.findByCustomerIdAndTransactionDateBetween(1L, day, day))
                .thenReturn(List.of(tx(c, "120.00", "2024-01-15")));

        Assertions.assertThat(service.getRewardsForCustomer(1L, day, day).totalPoints()).isEqualTo(90);
    }

    @Test
    void getRewardsForCustomer_transactionOnFromBoundaryIsIncluded() {
        Customer c = customer(1L, "Alice");
        LocalDate from = LocalDate.of(2024, 1, 15);
        LocalDate to   = LocalDate.of(2024, 1, 31);
        Mockito.when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        Mockito.when(transactionRepository.findByCustomerIdAndTransactionDateBetween(1L, from, to))
                .thenReturn(List.of(tx(c, "120.00", "2024-01-15")));

        Assertions.assertThat(service.getRewardsForCustomer(1L, from, to).totalPoints()).isEqualTo(90);
    }

    @Test
    void getRewardsForCustomer_transactionOnToBoundaryIsIncluded() {
        Customer c = customer(1L, "Alice");
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to   = LocalDate.of(2024, 1, 28);
        Mockito.when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        Mockito.when(transactionRepository.findByCustomerIdAndTransactionDateBetween(1L, from, to))
                .thenReturn(List.of(tx(c, "75.00", "2024-01-28")));

        Assertions.assertThat(service.getRewardsForCustomer(1L, from, to).totalPoints()).isEqualTo(25);
    }

    @Test
    void getRewardsPaged_returnsCorrectPageMetadata() {
        Customer alice = customer(1L, "Alice");
        Customer bob   = customer(2L, "Bob");
        var pageable = PageRequest.of(0, 2);
        Mockito.when(customerRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(alice, bob), pageable, 3));
        Mockito.when(transactionRepository.findByCustomerId(1L)).thenReturn(List.of());
        Mockito.when(transactionRepository.findByCustomerId(2L)).thenReturn(List.of());

        var result = service.getRewardsPaged(pageable, null, null);

        Assertions.assertThat(result.page()).isEqualTo(0);
        Assertions.assertThat(result.size()).isEqualTo(2);
        Assertions.assertThat(result.totalElements()).isEqualTo(3);
        Assertions.assertThat(result.totalPages()).isEqualTo(2);
        Assertions.assertThat(result.last()).isFalse();
        Assertions.assertThat(result.content()).hasSize(2);
    }

    @Test
    void getRewardsPaged_withDateFilter_callsDateRangeRepo() {
        Customer alice = customer(1L, "Alice");
        var pageable = PageRequest.of(0, 10);
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to   = LocalDate.of(2024, 1, 31);
        Mockito.when(customerRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(alice), pageable, 1));
        Mockito.when(transactionRepository.findByCustomerIdAndTransactionDateBetween(1L, from, to))
                .thenReturn(List.of(tx(alice, "120.00", "2024-01-15")));

        var result = service.getRewardsPaged(pageable, from, to);

        Assertions.assertThat(result.content().get(0).totalPoints()).isEqualTo(90);
        Mockito.verify(transactionRepository).findByCustomerIdAndTransactionDateBetween(1L, from, to);
    }

    @Test
    void getRewardsPaged_fromAfterTo_throwsInvalidDateRangeException() {
        Customer alice = customer(1L, "Alice");
        var pageable = PageRequest.of(0, 10);
        LocalDate from = LocalDate.of(2024, 3, 1);
        LocalDate to   = LocalDate.of(2024, 1, 1);
        Mockito.when(customerRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(alice), pageable, 1));

        Assertions.assertThatThrownBy(() -> service.getRewardsPaged(pageable, from, to))
                .isInstanceOf(InvalidDateRangeException.class);
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
