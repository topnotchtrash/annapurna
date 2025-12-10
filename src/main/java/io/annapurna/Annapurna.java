package io.annapurna;

import io.annapurna.generator.EquitySwapGenerator;
import io.annapurna.generator.TradeGenerator;
import io.annapurna.model.Trade;

/**
 * Main entry point for the Annapurna synthetic trade data generator.
 *
 *
 *
 * Trade trade = Annapurna.generateOne();
 *
 *
 * Advanced usage (coming soon):
 *
 * List&lt;Trade&gt; trades = Annapurna.builder()
 *     .tradeTypes()
 *         .equitySwap(100)
 *     .count(1000)
 *     .build()
 *     .generate();
 *
 */
public class Annapurna {

    /**
     * Generate a single equity swap trade with realistic data.
     *
     * @return A fully populated EquitySwap trade
     */
    public static Trade generateOne() {
        TradeGenerator generator = new EquitySwapGenerator();
        return generator.generate();
    }

    /**
     * Create a new builder for advanced configuration.
     * (Coming soon in Phase 3)
     *
     * @return AnnapurnaBuilder instance
     */
    public static Object builder() {
        throw new UnsupportedOperationException("Builder pattern coming in Phase 3");
    }

    /**
     * Quick create method for simple use cases.
     * (Coming soon in Phase 3)
     *
     * @return A simple generator instance
     */
    public static Object create() {
        throw new UnsupportedOperationException("Fluent API coming in Phase 3");
    }
}