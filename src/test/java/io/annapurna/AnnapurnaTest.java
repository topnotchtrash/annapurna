package io.annapurna;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.annapurna.model.EquitySwap;
import io.annapurna.model.Trade;
import io.annapurna.model.TradeType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Annapurna main API
 */
class AnnapurnaTest {

    @Test
    void testGenerateOneEquitySwap() {
        // Generate a single trade
        Trade trade = Annapurna.generateOne();

        // Basic assertions
        assertNotNull(trade, "Trade should not be null");
        assertTrue(trade instanceof EquitySwap, "Trade should be EquitySwap type");
        assertEquals(TradeType.EQUITY_SWAP, trade.getTradeType(), "Trade type should be EQUITY_SWAP");

        // Cast to EquitySwap for specific checks
        EquitySwap swap = (EquitySwap) trade;

        // Verify all critical fields are populated
        assertNotNull(swap.getTradeId(), "Trade ID should not be null");
        assertNotNull(swap.getTradeDate(), "Trade date should not be null");
        assertNotNull(swap.getSettlementDate(), "Settlement date should not be null");
        assertNotNull(swap.getMaturityDate(), "Maturity date should not be null");
        assertNotNull(swap.getReferenceAsset(), "Reference asset should not be null");
        assertNotNull(swap.getCounterparty(), "Counterparty should not be null");
        assertNotNull(swap.getNotional(), "Notional should not be null");
        assertNotNull(swap.getCurrency(), "Currency should not be null");

        // Verify notional is in expected range ($5M - $100M)
        assertTrue(swap.getNotional().compareTo(BigDecimal.valueOf(5_000_000)) >= 0,
                "Notional should be >= $5M");
        assertTrue(swap.getNotional().compareTo(BigDecimal.valueOf(100_000_000)) <= 0,
                "Notional should be <= $100M");

        // Verify funding spread is in expected range (50-200 bps)
        assertNotNull(swap.getFundingSpreadBps(), "Funding spread should not be null");
        assertTrue(swap.getFundingSpreadBps() >= 50 && swap.getFundingSpreadBps() <= 200,
                "Funding spread should be between 50-200 bps");

        // Verify date logic
        assertTrue(swap.getSettlementDate().isAfter(swap.getTradeDate()),
                "Settlement date should be after trade date");
        assertTrue(swap.getMaturityDate().isAfter(swap.getSettlementDate()),
                "Maturity date should be after settlement date");

        // Print the trade for manual inspection
        System.out.println("\n=== Generated Equity Swap ===");
        System.out.println(swap);
        System.out.println("=============================\n");
    }

    @Test
    void testGenerateMultipleTradesAreUnique() {
        // Generate multiple trades
        Trade trade1 = Annapurna.generateOne();
        Trade trade2 = Annapurna.generateOne();
        Trade trade3 = Annapurna.generateOne();

        // Verify they have unique IDs
        assertNotEquals(trade1.getTradeId(), trade2.getTradeId(),
                "Trade IDs should be unique");
        assertNotEquals(trade2.getTradeId(), trade3.getTradeId(),
                "Trade IDs should be unique");
        assertNotEquals(trade1.getTradeId(), trade3.getTradeId(),
                "Trade IDs should be unique");
    }

    @Test
    void testTradeJsonSerialization() throws Exception {
        // Generate a trade
        Trade trade = Annapurna.generateOne();

        // Setup Jackson ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Serialize to JSON
        String json = mapper.writeValueAsString(trade);

        // Verify JSON is not empty and contains expected fields
        assertNotNull(json, "JSON should not be null");
        assertTrue(json.contains("tradeId"), "JSON should contain tradeId");
        assertTrue(json.contains("referenceAsset"), "JSON should contain referenceAsset");
        assertTrue(json.contains("notional"), "JSON should contain notional");
        assertTrue(json.contains("EQUITY_SWAP"), "JSON should contain trade type");

        // Print JSON for manual inspection
        System.out.println("\n=== Trade as JSON ===");
        System.out.println(json);
        System.out.println("=====================\n");

        // Verify we can deserialize back
        Trade deserialized = mapper.readValue(json, Trade.class);
        assertNotNull(deserialized, "Deserialized trade should not be null");
        assertEquals(trade.getTradeId(), deserialized.getTradeId(),
                "Deserialized trade should have same ID");
    }

    @Test
    void testGenerateMultipleTrades() {
        // Generate 10 trades and verify all are valid
        for (int i = 0; i < 10; i++) {
            Trade trade = Annapurna.generateOne();
            assertNotNull(trade, "Trade " + i + " should not be null");
            assertTrue(trade instanceof EquitySwap, "Trade " + i + " should be EquitySwap");
        }

        System.out.println(" Successfully generated 10 equity swaps");
    }
}