package io.annapurna;

import io.annapurna.generator.EquitySwapGenerator;
import io.annapurna.generator.TradeGenerator;
import io.annapurna.model.Trade;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Main entry point for the Annapurna synthetic trade data generator.
 *
 * <p>Simple usage:
 * <pre>
 * Trade trade = Annapurna.generateOne();
 * </pre>
 *
 * <p>Bulk generation with defaults:
 * <pre>
 * List&lt;Trade&gt; trades = Annapurna.generate(10000);
 * </pre>
 *
 * <p>Advanced configuration:
 * <pre>
 * List&lt;Trade&gt; trades = Annapurna.builder()
 *     .tradeTypes()
 *         .equitySwap(30)
 *         .interestRateSwap(30)
 *         .fxForward(20)
 *         .option(15)
 *         .cds(5)
 *     .count(100000)
 *     .parallelism(8)
 *     .build()
 *     .generate();
 * </pre>
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
     * Generate multiple trades with default equal distribution.
     * Uses all CPU cores for parallel generation.
     *
     * @param count Number of trades to generate
     * @return List of mixed trade types
     */
    public static List<Trade> generate(int count) {
        return builder()
                .count(count)
                .build()
                .generate();
    }

    /**
     * Create a builder for advanced configuration.
     *
     * @return AnnapurnaBuilder instance for fluent configuration
     */
    public static AnnapurnaBuilder builder() {
        return new AnnapurnaBuilder();
    }
}