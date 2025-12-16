package io.annapurna;

import io.annapurna.generator.EquitySwapGenerator;
import io.annapurna.generator.TradeGenerator;
import io.annapurna.model.Trade;

import java.util.List;

/**
 * Main entry point for the Annapurna synthetic trade data generator.
 *
 * <p>Annapurna generates realistic financial trade data across 5 asset classes:
 * <ul>
 *   <li>Equity Swaps</li>
 *   <li>Interest Rate Swaps</li>
 *   <li>FX Forwards</li>
 *   <li>Equity Options</li>
 *   <li>Credit Default Swaps</li>
 * </ul>
 *
 * <p><b>Simple Usage:</b>
 * <pre>{@code
 * // Generate a single trade
 * Trade trade = Annapurna.generateOne();
 *
 * // Generate 10,000 trades with default distribution
 * List<Trade> trades = Annapurna.generate(10000);
 * }</pre>
 *
 * <p><b>Advanced Configuration:</b>
 * <pre>{@code
 * List<Trade> trades = Annapurna.builder()
 *     .tradeTypes()
 *         .equitySwap(30)
 *         .interestRateSwap(30)
 *         .fxForward(20)
 *         .option(15)
 *         .cds(5)
 *     .dataProfile()
 *         .clean(70)
 *         .edgeCase(20)
 *         .stress(10)
 *     .count(100000)
 *     .parallelism(8)
 *     .build()
 *     .generate();
 * }</pre>
 *
 * <p><b>Performance:</b>
 * Generates 680,000+ trades per second with parallel execution enabled.
 * Uses ThreadLocalRandom for lock-free thread-safe operation.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class Annapurna {

    /**
     * Generate a single equity swap trade with realistic data.
     *
     * <p>This is a convenience method for quick testing. For production use
     * or bulk generation, use {@link #generate(int)} or {@link #builder()}.
     *
     * <p>The generated trade will have:
     * <ul>
     *   <li>Realistic notional ($5M - $100M range)</li>
     *   <li>Market-weighted reference asset selection</li>
     *   <li>Proper counterparty matching based on deal size</li>
     *   <li>Mathematically correct notional = quantity × price</li>
     *   <li>All dates in valid sequence (trade &lt; settlement &lt; maturity)</li>
     * </ul>
     *
     * @return A fully populated EquitySwap trade with realistic data
     */
    public static Trade generateOne() {
        TradeGenerator generator = new EquitySwapGenerator();
        return generator.generate();
    }

    /**
     * Generate multiple trades with default equal distribution across all 5 trade types.
     *
     * <p>Uses parallel generation across all available CPU cores for maximum performance.
     * Default distribution: 20% each of Equity Swaps, Interest Rate Swaps, FX Forwards,
     * Equity Options, and Credit Default Swaps.
     *
     * <p><b>Performance:</b> Generates 680,000+ trades/second on 8-core CPU.
     *
     * <p><b>Thread Safety:</b> This method is thread-safe and can be called concurrently.
     *
     * @param count Number of trades to generate (must be positive)
     * @return List of mixed trade types, randomly distributed
     * @throws IllegalArgumentException if count is less than or equal to 0
     *
     * @see #builder() for custom trade type distributions
     */
    public static List<Trade> generate(int count) {
        return builder()
                .count(count)
                .build()
                .generate();
    }

    /**
     * Create a builder for advanced configuration of trade generation.
     *
     * <p>The builder provides a fluent API for configuring:
     * <ul>
     *   <li><b>Trade Type Distribution:</b> Specify percentage of each trade type</li>
     *   <li><b>Data Quality Profiles:</b> Generate clean, edge-case, or stress-test data</li>
     *   <li><b>Parallelism:</b> Control number of threads used for generation</li>
     *   <li><b>Count:</b> Total number of trades to generate</li>
     * </ul>
     *
     * <p><b>Example - Custom Distribution:</b>
     * <pre>{@code
     * List<Trade> trades = Annapurna.builder()
     *     .tradeTypes()
     *         .equitySwap(40)
     *         .interestRateSwap(30)
     *         .fxForward(30)
     *     .count(50000)
     *     .build()
     *     .generate();
     * }</pre>
     *
     * <p><b>Example - Data Quality Profiles:</b>
     * <pre>{@code
     * List<Trade> trades = Annapurna.builder()
     *     .tradeTypes()
     *         .equitySwap(100)
     *     .dataProfile()
     *         .clean(70)      // 70% valid data
     *         .edgeCase(20)   // 20% edge cases
     *         .stress(10)     // 10% broken data
     *     .count(10000)
     *     .build()
     *     .generate();
     * }</pre>
     *
     * @return AnnapurnaBuilder instance for fluent configuration
     */
    public static AnnapurnaBuilder builder() {
        return new AnnapurnaBuilder();
    }
}