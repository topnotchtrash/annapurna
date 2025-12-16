package io.annapurna.profile;

import io.annapurna.Annapurna;
import io.annapurna.model.Trade;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileApplierTest {

    @Test
    void testDataProfilesDistribution() {
        // Generate 1000 trades with profile distribution
        List<Trade> trades = Annapurna.builder()
                .tradeTypes()
                .equitySwap(100)  // Only equity swaps for simplicity
                .dataProfile()
                .clean(70)
                .edgeCase(20)
                .stress(10)
                .count(1000)
                .build()
                .generate();

        assertEquals(1000, trades.size());

        // Count corrupted trades
        long cleanTrades = trades.stream()
                .filter(t -> t.getCounterparty() != null &&
                        t.getTrader() != null &&
                        t.getCurrency() != null &&
                        t.getNotional() != null &&
                        t.getNotional().compareTo(BigDecimal.valueOf(1000)) > 0)
                .count();

        long corruptedTrades = 1000 - cleanTrades;

        System.out.println("Clean-looking trades: " + cleanTrades);
        System.out.println("Corrupted trades: " + corruptedTrades);

        // Should have roughly 30% corrupted (20% edge + 10% stress)
        // Allow tolerance of ±10%
        assertTrue(corruptedTrades >= 200 && corruptedTrades <= 400,
                "Expected 200-400 corrupted trades, got: " + corruptedTrades);
    }

    @Test
    void testDataProfilesEdgeCases() {
        List<Trade> trades = Annapurna.builder()
                .tradeTypes()
                .equitySwap(100)
                .dataProfile()
                .edgeCase(100)  // 100% edge cases
                .count(100)
                .build()
                .generate();

        // At least some should have corruptions
        long withCorruptions = trades.stream()
                .filter(t -> t.getCounterparty() == null ||
                        t.getTrader() == null ||
                        t.getBook() == null ||
                        t.getNotional().compareTo(BigDecimal.valueOf(10)) < 0 ||
                        t.getNotional().compareTo(BigDecimal.valueOf(1_000_000_000L)) > 0)
                .count();

        System.out.println("Edge case corruptions: " + withCorruptions + " out of 100");

        assertTrue(withCorruptions > 50, "Expected at least 50% edge cases, got: " + withCorruptions);
    }

    @Test
    void testDataProfilesStress() {
        List<Trade> trades = Annapurna.builder()
                .tradeTypes()
                .equitySwap(100)
                .dataProfile()
                .stress(100)  // 100% stress
                .count(100)
                .build()
                .generate();

        // All should have critical corruptions
        long criticalCorruptions = trades.stream()
                .filter(t -> t.getCurrency() == null ||
                        t.getTradeDate() == null ||
                        t.getTradeId() == null ||
                        t.getNotional() == null ||
                        (t.getNotional() != null && t.getNotional().compareTo(BigDecimal.ZERO) <= 0))
                .count();

        System.out.println("Critical corruptions: " + criticalCorruptions + " out of 100");

        assertTrue(criticalCorruptions > 80, "Expected at least 80% critical corruptions, got: " + criticalCorruptions);
    }
}