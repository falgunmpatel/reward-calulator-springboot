package com.example.rewardcalculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests for {@link com.example.rewardcalculator.controller.RewardController}.
 *
 * <p>Uses an in-memory H2 database (Spring profile {@code test}) seeded by {@code data.sql}.
 * The full Spring application context is loaded with a mock servlet environment — no actual
 * HTTP port is opened. {@link MockMvc} is built from the {@link WebApplicationContext} and
 * used to fire requests and assert responses.</p>
 *
 * <p>Expected seed totals:
 * <ul>
 *   <li>Alice Johnson (id=1): 435 points</li>
 *   <li>Bob Smith     (id=2): 314 points</li>
 *   <li>Carol White   (id=3): 688 points</li>
 * </ul>
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class RewardControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    /**
     * Builds the {@link MockMvc} instance from the Spring web application context
     * before each test.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // -------------------------------------------------------------------------
    // GET /api/rewards — all customers
    // -------------------------------------------------------------------------

    /**
     * GET /api/rewards returns HTTP 200 with a JSON array of at least 3 entries.
     */
    @Test
    void testGetAllRewards_returnsOk() throws Exception {
        mockMvc.perform(get("/api/rewards").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    /**
     * GET /api/rewards response contains all three seeded customers by name.
     */
    @Test
    void testGetAllRewards_allCustomersPresent() throws Exception {
        mockMvc.perform(get("/api/rewards").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].customerName",
                        hasItems("Alice Johnson", "Bob Smith", "Carol White")));
    }

    /**
     * Each customer summary in the all-customers response includes a non-empty monthlyRewards list.
     */
    @Test
    void testGetAllRewards_eachCustomerHasMonthlyBreakdown() throws Exception {
        mockMvc.perform(get("/api/rewards").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].monthlyRewards", not(empty())))
                .andExpect(jsonPath("$[1].monthlyRewards", not(empty())))
                .andExpect(jsonPath("$[2].monthlyRewards", not(empty())));
    }

    // -------------------------------------------------------------------------
    // GET /api/rewards/{customerId} — single customer
    // -------------------------------------------------------------------------

    /**
     * GET /api/rewards/1 returns HTTP 200 with customerId = 1.
     */
    @Test
    void testGetCustomerReward_returnsOk() throws Exception {
        mockMvc.perform(get("/api/rewards/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId", is(1)));
    }

    /**
     * GET /api/rewards/1 returns the correct customer name.
     */
    @Test
    void testGetCustomerReward_correctName() throws Exception {
        mockMvc.perform(get("/api/rewards/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName", is("Alice Johnson")));
    }

    /**
     * GET /api/rewards/1 returns a non-empty monthlyRewards array.
     */
    @Test
    void testGetCustomerReward_monthlyBreakdown() throws Exception {
        mockMvc.perform(get("/api/rewards/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyRewards", not(empty())));
    }

    /**
     * GET /api/rewards/1 returns Alice's expected total of 435 points.
     * Jan: $120→(20×2)+50=90, $75.50→25=25 → 115
     * Feb: $200→(100×2)+50=250, $45→0 → 250
     * Mar: $110→(10×2)+50=70 → 70
     * Total: 115+250+70=435.
     */
    @Test
    void testGetCustomerReward_totalPoints_alice() throws Exception {
        mockMvc.perform(get("/api/rewards/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints", is(435)));
    }

    /**
     * GET /api/rewards/2 returns Bob's expected total of 314 points.
     * Jan: $55→5, $130→(30×2)+50=110 → 115
     * Feb: $99.99→49, $40→0 → 49
     * Mar: $150→(50×2)+50=150 → 150
     * Total: 115+49+150=314.
     */
    @Test
    void testGetCustomerReward_totalPoints_bob() throws Exception {
        mockMvc.perform(get("/api/rewards/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints", is(314)));
    }

    /**
     * GET /api/rewards/3 returns Carol's expected total of 688 points.
     * Jan: $300 → (200×2)+50=450, Feb: $88(38)+$50(0)=38, Mar: $175 → (75×2)+50=200 → total=688.
     */
    @Test
    void testGetCustomerReward_totalPoints_carol() throws Exception {
        mockMvc.perform(get("/api/rewards/3").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints", is(688)));
    }

    /**
     * GET /api/rewards/1 returns 3 monthly entries in chronological order (Jan, Feb, Mar).
     */
    @Test
    void testGetCustomerReward_monthlyOrder() throws Exception {
        mockMvc.perform(get("/api/rewards/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyRewards", hasSize(3)))
                .andExpect(jsonPath("$.monthlyRewards[0].month", is("JANUARY")))
                .andExpect(jsonPath("$.monthlyRewards[1].month", is("FEBRUARY")))
                .andExpect(jsonPath("$.monthlyRewards[2].month", is("MARCH")));
    }

    // -------------------------------------------------------------------------
    // Error / negative scenarios
    // -------------------------------------------------------------------------

    /**
     * GET /api/rewards/999 returns HTTP 404 with a body containing the missing ID.
     */
    @Test
    void testGetCustomerReward_notFound() throws Exception {
        mockMvc.perform(get("/api/rewards/999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("999")));
    }

    /**
     * GET /api/rewards/abc returns HTTP 400 because "abc" cannot be parsed as a Long.
     */
    @Test
    void testGetCustomerReward_invalidIdType() throws Exception {
        mockMvc.perform(get("/api/rewards/abc").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }
}
