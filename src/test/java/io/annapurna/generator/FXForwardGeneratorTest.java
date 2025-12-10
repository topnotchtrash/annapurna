package io.annapurna.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.annapurna.generator.FXForwardGenerator;
import io.annapurna.model.Currency;
import io.annapurna.model.FXForward;
import io.annapurna.model.Trade;
import io.annapurna.model.TradeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for FX Forward Generator validation.
 */
class FXForwardGeneratorTest {

    private FXForwardGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new FXForwardGenerator();
    }

    @Test
    void testGenerateOneFXForward() {
        Trade trade = generator.generate();

        // Basic assertions
        assertNotNull(trade, "Trade should not be null");
        assertTrue(trade instanceof FXForward, "Trade should be FXForward type");
        assertEquals(TradeType.FX_FORWARD, trade.getTradeType(), "Trade type should be FX_FORWARD");

        FXForward fx = (FXForward) trade;

        // Verify critical fields
        assertNotNull(fx.getCurrencyPair(), "Currency Pair should not be null");
        assertNotNull(fx.getSpotRate(), "Spot Rate should not be null");
        assertNotNull(fx.getForwardRate(), "Forward Rate should not be null");
        assertNotNull(fx.getForwardPoints(), "Forward Points should not be null");
        assertNotNull(fx.getSettlementType(), "Settlement Type should not be null");

        // Verify settlement type is Physical (as per updated logic)
        assertEquals("PHYSICAL", fx.getSettlementType(), "Settlement type should be PHYSICAL for majors");

        System.out.println("--- Generated FX Trade ---");
        System.out.println("Pair: " + fx.getCurrencyPair());
        System.out.println("Spot: " + fx.getSpotRate());
        System.out.println("Fwd:  " + fx.getForwardRate());
        System.out.println("--------------------------");
    }

    @Test
    void testJpyScalingLogic() {
        // Generate trades until we get a JPY pair to verify specific formatting
        boolean foundJpy = false;

        // Try up to 100 times to find a JPY trade (USD/JPY, EUR/JPY, etc.)
        for (int i = 0; i < 100; i++) {
            FXForward fx = (FXForward) generator.generate();
            if (fx.getCurrencyPair().contains("JPY")) {
                foundJpy = true;

                // Assert Spot Rate has 2 decimal places (e.g., 145.50)
                assertEquals(2, fx.getSpotRate().scale(),
                        "JPY Spot Rate should have scale of 2");

                // Assert the rate is realistically large (> 100)
                assertTrue(fx.getSpotRate().compareTo(BigDecimal.valueOf(100)) > 0,
                        "JPY Rate should be > 100");

                break; // Test passed, exit loop
            }
        }

        if (!foundJpy) {
            fail("Could not generate a JPY pair in 100 attempts - check generator weights");
        }
    }

    @Test
    void testStandardPairScalingLogic() {
        // Generate trades until we find EUR/USD or GBP/USD
        boolean foundStandard = false;

        for (int i = 0; i < 100; i++) {
            FXForward fx = (FXForward) generator.generate();
            if (fx.getCurrencyPair().equals("EUR/USD")) {
                foundStandard = true;

                // Assert Spot Rate has 4 decimal places (e.g., 1.0850)
                assertEquals(4, fx.getSpotRate().scale(),
                        "EUR/USD Spot Rate should have scale of 4");

                // Assert rate is small (< 2.0)
                assertTrue(fx.getSpotRate().compareTo(BigDecimal.valueOf(2.0)) < 0,
                        "EUR/USD Rate should be < 2.0");

                break;
            }
        }

        if (!foundStandard) {
            fail("Could not generate EUR/USD in 100 attempts");
        }
    }

    @Test
    void testForwardPointsMath() {
        // Validate that Spot, Forward, and Points are mathematically consistent
        FXForward fx = (FXForward) generator.generate();

        BigDecimal spot = fx.getSpotRate();
        BigDecimal fwd = fx.getForwardRate();
        BigDecimal points = fx.getForwardPoints();

        // Logic check: If Forward > Spot, Points must be positive
        if (fwd.compareTo(spot) > 0) {
            assertTrue(points.compareTo(BigDecimal.ZERO) > 0,
                    "If Forward > Spot, Points must be positive");
        } else if (fwd.compareTo(spot) < 0) {
            assertTrue(points.compareTo(BigDecimal.ZERO) < 0,
                    "If Forward < Spot, Points must be negative");
        } else {
            assertEquals(0, points.compareTo(BigDecimal.ZERO),
                    "If Forward == Spot, Points must be zero");
        }
    }

    @Test
    void testDateLogic() {
        FXForward fx = (FXForward) generator.generate();

        // 1. Maturity must be after Trade Date
        assertTrue(fx.getMaturityDate().isAfter(fx.getTradeDate()),
                "Maturity date must be after trade date");

        // 2. In standard FX Forwards, Settlement Date usually equals Maturity Date
        assertEquals(fx.getSettlementDate(), fx.getMaturityDate(),
                "Settlement Date should match Maturity Date for FX Forwards");
    }

    @Test
    void testNotionalRange() {
        FXForward fx = (FXForward) generator.generate();

        // Range: $500K - $200M
        BigDecimal min = BigDecimal.valueOf(500_000);
        BigDecimal max = BigDecimal.valueOf(200_000_000);

        assertTrue(fx.getNotional().compareTo(min) >= 0, "Notional should be >= 500k");
        assertTrue(fx.getNotional().compareTo(max) <= 0, "Notional should be <= 200M");
    }

    @Test
    void testCrossPairConfiguration() {
        // Look for a specific cross pair to ensure base/quote logic is correct
        for (int i = 0; i < 100; i++) {
            FXForward fx = (FXForward) generator.generate();
            if (fx.getCurrencyPair().equals("EUR/GBP")) {
                assertEquals(Currency.EUR, fx.getBaseCurrency(), "Base currency for EUR/GBP should be EUR");
                assertEquals(Currency.GBP, fx.getQuoteCurrency(), "Quote currency for EUR/GBP should be GBP");
                return;
            }
        }
    }

    @Test
    void testJsonSerialization() throws Exception {
        Trade trade = generator.generate();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String json = mapper.writeValueAsString(trade);

        assertNotNull(json, "JSON should not be null");
        assertTrue(json.contains("currencyPair"), "JSON should contain currencyPair");
        assertTrue(json.contains("spotRate"), "JSON should contain spotRate");
        assertTrue(json.contains("forwardPoints"), "JSON should contain forwardPoints");

        System.out.println("--- FX JSON Output ---");
        System.out.println(json);
        System.out.println("----------------------");
    }

    @Test
    void testUniqueIds() {
        Trade t1 = generator.generate();
        Trade t2 = generator.generate();

        assertNotEquals(t1.getTradeId(), t2.getTradeId(), "Trade IDs must be unique");
    }
}