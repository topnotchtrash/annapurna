package io.annapurna.generator;

import io.annapurna.model.TradeType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Factory for creating trade generators based on configured distribution.
 * Thread-safe for parallel execution.
 */
public class TradeFactory {

    private final Map<TradeType, Integer> distribution;
    private final int totalWeight;

    /**
     * Create factory with trade type distribution.
     *
     * @param distribution Map of TradeType to percentage (0-100)
     */
    public TradeFactory(Map<TradeType, Integer> distribution) {
        this.distribution = new HashMap<>(distribution);
        this.totalWeight = distribution.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        if (totalWeight != 100) {
            throw new IllegalArgumentException(
                    "Trade type percentages must sum to 100, got: " + totalWeight
            );
        }
    }

    /**
     * Create a generator based on weighted random selection.
     * Thread-safe - uses ThreadLocalRandom.
     *
     * @return A new TradeGenerator instance
     */
    public TradeGenerator createGenerator() {
        int random = ThreadLocalRandom.current().nextInt(100);
        int cumulative = 0;

        for (Map.Entry<TradeType, Integer> entry : distribution.entrySet()) {
            cumulative += entry.getValue();
            if (random < cumulative) {
                return createGeneratorForType(entry.getKey());
            }
        }

        // Fallback (should never happen)
        return new EquitySwapGenerator();
    }

    /**
     * Create a specific generator for a trade type.
     */
    private TradeGenerator createGeneratorForType(TradeType type) {
        switch (type) {
            case EQUITY_SWAP:
                return new EquitySwapGenerator();
            case INTEREST_RATE_SWAP:
                return new InterestRateSwapGenerator();
            case FX_FORWARD:
                return new FXForwardGenerator();
            case EQUITY_OPTION:
                return new EquityOptionGenerator();
            case CREDIT_DEFAULT_SWAP:
                return new CDSGenerator();
            default:
                throw new IllegalArgumentException("Unknown trade type: " + type);
        }
    }
}