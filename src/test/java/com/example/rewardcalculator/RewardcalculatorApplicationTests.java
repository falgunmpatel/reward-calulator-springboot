package com.example.rewardcalculator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test that verifies the Spring application context loads successfully
 * against the in-memory H2 test database.
 */
@SpringBootTest
@ActiveProfiles("test")
class RewardcalculatorApplicationTests {

    @Test
    void contextLoads() {
    }
}
