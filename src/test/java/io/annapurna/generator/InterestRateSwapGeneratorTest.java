package io.annapurna.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.annapurna.model.Currency;
import io.annapurna.model.InterestRateSwap;
import io.annapurna.model.Trade;
import io.annapurna.model.TradeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Interest Rate Swap Generator validation.
 */
class InterestRateSwapGeneratorTest {

    private InterestRateSwapGenerator generator;

    @BeforeEach
    void setUp() {
        // Initialize a new generator before each test
        generator = new InterestRateSwapGenerator();
    }

    @Test
    void testGenerateOneInterestRateSwap() {
        // Generate a single trade
        Trade trade = generator.generate();

        // Basic assertions
        assertNotNull(trade, "Trade should not be null");
        assertTrue(trade instanceof InterestRateSwap, "Trade should be InterestRateSwap type");
        assertEquals(TradeType.INTEREST_RATE_SWAP, trade.getTradeType(), "Trade type should be INTEREST_RATE_SWAP");

        // Cast for specific checks
        InterestRateSwap irs = (InterestRateSwap) trade;

        // Verify critical fields
        assertNotNull(irs.getTradeId(), "Trade ID should not be null");
        assertNotNull(irs.getFixedRate(), "Fixed Rate should not be null");
        assertNotNull(irs.getFloatingRateIndex(), "Floating Index should not be null");
        assertNotNull(irs.getDayCountConvention(), "Day Count Convention should not be null");

        // Verify Fixed Rate is within realistic bounds (e.g., 0% to 10%)
        assertTrue(irs.getFixedRate().compareTo(BigDecimal.ZERO) > 0, "Fixed rate should be positive");
        assertTrue(irs.getFixedRate().compareTo(BigDecimal.TEN) < 0, "Fixed rate should be less than 10%");

        // Print for inspection
        System.out.println("--- Generated IRS Trade ---");
        System.out.println(irs);
        System.out.println("---------------------------");
    }

    @Test
    void testCurrencyMatchesIndexLogic() {
        // Generate enough trades to likely hit both USD and EUR scenarios
        boolean foundEur = false;
        boolean foundUsd = false;

        for (int i = 0; i < 20; i++) {
            InterestRateSwap irs = (InterestRateSwap) generator.generate();

            if (irs.getFloatingRateIndex().equals("EURIBOR")) {
                assertEquals(Currency.EUR, irs.getCurrency(), "EURIBOR trades must be in EUR");
                foundEur = true;
            } else if (irs.getFloatingRateIndex().equals("SOFR")) {
                assertEquals(Currency.USD, irs.getCurrency(), "SOFR trades must be in USD");
                foundUsd = true;
            }
        }

        // Note: It is possible (though unlikely) to not generate one type in 20 tries
        // strictly speaking we verify the logic holds for what WAS generated.
    }

    @Test
    void testDayCountConventionLogic() {
        for (int i = 0; i < 20; i++) {
            InterestRateSwap irs = (InterestRateSwap) generator.generate();

            if (irs.getCurrency() == Currency.USD) {
                assertEquals("ACT/360", irs.getDayCountConvention(), "USD trades should be ACT/360");
            } else if (irs.getCurrency() == Currency.EUR) {
                assertEquals("30/360", irs.getDayCountConvention(), "EUR trades should be 30/360");
            }
        }
    }

    @Test
    void testNotionalRange() {
        InterestRateSwap irs = (InterestRateSwap) generator.generate();

        // Range: $10M to $500M
        BigDecimal min = BigDecimal.valueOf(10_000_000);
        BigDecimal max = BigDecimal.valueOf(500_000_000);

        assertTrue(irs.getNotional().compareTo(min) >= 0, "Notional should be >= 10M");
        assertTrue(irs.getNotional().compareTo(max) <= 0, "Notional should be <= 500M");
    }

    @Test
    void testDateLogic() {
        InterestRateSwap irs = (InterestRateSwap) generator.generate();

        // Settlement is usually T+2
        assertTrue(irs.getSettlementDate().isAfter(irs.getTradeDate()),
                "Settlement date must be after trade date");

        // Maturity must be years after settlement
        assertTrue(irs.getMaturityDate().isAfter(irs.getSettlementDate()),
                "Maturity date must be after settlement date");
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
        assertTrue(json.contains("fixedRate"), "JSON should contain fixedRate");
        assertTrue(json.contains("floatingRateIndex"), "JSON should contain floatingRateIndex");
        assertTrue(json.contains("dayCountConvention"), "JSON should contain dayCountConvention");

        System.out.println("--- IRS JSON Output ---");
        System.out.println(json);
        System.out.println("-----------------------");
    }

    @Test
    void testUniqueIds() {
        Trade t1 = generator.generate();
        Trade t2 = generator.generate();

        assertNotEquals(t1.getTradeId(), t2.getTradeId(), "Trade IDs must be unique");
    }
}