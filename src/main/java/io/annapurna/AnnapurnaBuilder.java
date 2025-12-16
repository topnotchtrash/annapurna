package io.annapurna;

import io.annapurna.config.GeneratorConfig;
import io.annapurna.generator.TradeFactory;
import io.annapurna.model.Trade;
import io.annapurna.model.TradeType;
import io.annapurna.profile.DataProfile;
import io.annapurna.profile.ProfileApplier;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Fluent builder for configuring bulk trade generation with advanced options.
 *
 * <p>Provides a type-safe, fluent API for configuring:
 * <ul>
 *   <li>Trade type distribution (percentages must sum to 100)</li>
 *   <li>Data quality profiles (clean, edge-case, stress)</li>
 *   <li>Parallelism level (number of threads)</li>
 *   <li>Total count of trades to generate</li>
 * </ul>
 *
 * <p><b>Basic Usage:</b>
 * <pre>{@code
 * List<Trade> trades = Annapurna.builder()
 *     .tradeTypes()
 *         .equitySwap(50)
 *         .interestRateSwap(50)
 *     .count(10000)
 *     .build()
 *     .generate();
 * }</pre>
 *
 * <p><b>Advanced Usage with Profiles:</b>
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
 * <p><b>Thread Safety:</b> The builder itself is not thread-safe, but the generated
 * trades are produced using thread-safe generators.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class AnnapurnaBuilder {

    private int count = 1000;
    private Map<TradeType, Integer> tradeTypeDistribution = new HashMap<>();
    private int parallelism = Runtime.getRuntime().availableProcessors();
    private LocalDate startDate = LocalDate.now().minusYears(1);
    private LocalDate endDate = LocalDate.now();
    private Map<DataProfile, Integer> profileDistribution = new HashMap<>();
    private boolean useProfiles = false;

    AnnapurnaBuilder() {
        // Package-private constructor
        setDefaultDistribution();
    }

    private void setDefaultDistribution() {
        tradeTypeDistribution.put(TradeType.EQUITY_SWAP, 20);
        tradeTypeDistribution.put(TradeType.INTEREST_RATE_SWAP, 20);
        tradeTypeDistribution.put(TradeType.FX_FORWARD, 20);
        tradeTypeDistribution.put(TradeType.EQUITY_OPTION, 20);
        tradeTypeDistribution.put(TradeType.CREDIT_DEFAULT_SWAP, 20);
    }
    /**
     * Configure trade type distribution.
     *
     * <p>Returns a {@link TradeTypeBuilder} for specifying the percentage
     * of each trade type. Percentages must sum to exactly 100.
     *
     * @return TradeTypeBuilder for fluent configuration
     */
    public TradeTypeBuilder tradeTypes() {
        return new TradeTypeBuilder(this);
    }

    /**
     * Configure data quality profile distribution for testing scenarios.
     *
     * <p>Data quality profiles allow generation of intentionally corrupted data
     * for testing error handling:
     * <ul>
     *   <li><b>CLEAN:</b> All fields valid, realistic values</li>
     *   <li><b>EDGE_CASE:</b> Valid but unusual (missing optional fields, extreme values)</li>
     *   <li><b>STRESS:</b> Invalid data (null required fields, negative notionals)</li>
     * </ul>
     *
     * @return DataProfileBuilder for fluent configuration
     */
    public DataProfileBuilder dataProfile() {
        this.useProfiles = true;
        return new DataProfileBuilder(this);
    }

    /**
     * Set total number of trades to generate.
     */
    public AnnapurnaBuilder count(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        this.count = count;
        return this;
    }

    /**
     * Set parallelism level (number of threads to use for generation).
     *
     * <p>Default is the number of available CPU cores. Higher parallelism
     * improves performance up to the number of physical cores.
     *
     * <p><b>Performance Guide:</b>
     * <ul>
     *   <li>1 thread: ~400K trades/sec</li>
     *   <li>8 threads: ~750K trades/sec</li>
     *   <li>16 threads: ~770K trades/sec (diminishing returns)</li>
     * </ul>
     *
     * @param parallelism Number of threads (must be positive)
     * @return this builder for method chaining
     * @throws IllegalArgumentException if parallelism is less than or equal to 0
     */
    public AnnapurnaBuilder parallelism(int parallelism) {
        if (parallelism <= 0) {
            throw new IllegalArgumentException("Parallelism must be positive");
        }
        this.parallelism = parallelism;
        return this;
    }

    /**
     * Set date range for generated trades.
     */
    public AnnapurnaBuilder dateRange(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        return this;
    }

    /**
     * Build the generator and execute generation.
     */
    public BulkTradeGenerator build() {
        // Validate distribution
        int total = tradeTypeDistribution.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        if (total != 100) {
            throw new IllegalArgumentException(
                    "Trade type percentages must sum to 100, got: " + total
            );
        }

        GeneratorConfig config = GeneratorConfig.builder()
                .count(count)
                .tradeTypeDistribution(tradeTypeDistribution)
                .parallelism(parallelism)
                .dateRange(startDate, endDate)
                .profileDistribution(profileDistribution)
                .useProfiles(useProfiles)
                .build();

        return new BulkTradeGenerator(config);
    }

    /**
     * Inner builder for configuring trade type distribution.
     */
    public static class TradeTypeBuilder {
        private final AnnapurnaBuilder parent;

        TradeTypeBuilder(AnnapurnaBuilder parent) {
            this.parent = parent;
            // Clear existing distribution
            parent.tradeTypeDistribution.clear();
        }

        public TradeTypeBuilder equitySwap(int percentage) {
            validatePercentage(percentage);
            parent.tradeTypeDistribution.put(TradeType.EQUITY_SWAP, percentage);
            return this;
        }

        public TradeTypeBuilder interestRateSwap(int percentage) {
            validatePercentage(percentage);
            parent.tradeTypeDistribution.put(TradeType.INTEREST_RATE_SWAP, percentage);
            return this;
        }

        public TradeTypeBuilder fxForward(int percentage) {
            validatePercentage(percentage);
            parent.tradeTypeDistribution.put(TradeType.FX_FORWARD, percentage);
            return this;
        }

        public TradeTypeBuilder option(int percentage) {
            validatePercentage(percentage);
            parent.tradeTypeDistribution.put(TradeType.EQUITY_OPTION, percentage);
            return this;
        }

        public TradeTypeBuilder cds(int percentage) {
            validatePercentage(percentage);
            parent.tradeTypeDistribution.put(TradeType.CREDIT_DEFAULT_SWAP, percentage);
            return this;
        }

        private void validatePercentage(int percentage) {
            if (percentage < 0 || percentage > 100) {
                throw new IllegalArgumentException(
                        "Percentage must be between 0 and 100, got: " + percentage
                );
            }
        }

        /**
         * Return to parent builder to continue configuration.
         */
        public AnnapurnaBuilder count(int count) {
            return parent.count(count);
        }

        public AnnapurnaBuilder parallelism(int parallelism) {
            return parent.parallelism(parallelism);
        }

        public AnnapurnaBuilder dateRange(LocalDate startDate, LocalDate endDate) {
            return parent.dateRange(startDate, endDate);
        }

        public DataProfileBuilder dataProfile() {
            return parent.dataProfile();
        }

        public BulkTradeGenerator build() {
            return parent.build();
        }
    }

    /**
     * Inner builder for configuring data quality profile distribution.
     */
    public static class DataProfileBuilder {
        private final AnnapurnaBuilder parent;

        DataProfileBuilder(AnnapurnaBuilder parent) {
            this.parent = parent;
            // Clear existing distribution
            parent.profileDistribution.clear();
        }

        /**
         * Set percentage of CLEAN trades (perfect data).
         */
        public DataProfileBuilder clean(int percentage) {
            validatePercentage(percentage);
            parent.profileDistribution.put(DataProfile.CLEAN, percentage);
            return this;
        }

        /**
         * Set percentage of EDGE_CASE trades (valid but unusual).
         */
        public DataProfileBuilder edgeCase(int percentage) {
            validatePercentage(percentage);
            parent.profileDistribution.put(DataProfile.EDGE_CASE, percentage);
            return this;
        }

        /**
         * Set percentage of STRESS trades (invalid/broken).
         */
        public DataProfileBuilder stress(int percentage) {
            validatePercentage(percentage);
            parent.profileDistribution.put(DataProfile.STRESS, percentage);
            return this;
        }

        private void validatePercentage(int percentage) {
            if (percentage < 0 || percentage > 100) {
                throw new IllegalArgumentException(
                        "Percentage must be between 0 and 100, got: " + percentage
                );
            }
        }

        /**
         * Return to parent builder to continue configuration.
         */
        public AnnapurnaBuilder count(int count) {
            return parent.count(count);
        }

        public AnnapurnaBuilder parallelism(int parallelism) {
            return parent.parallelism(parallelism);
        }

        public BulkTradeGenerator build() {
            return parent.build();
        }
    }

    /**
     * Bulk trade generator that executes parallel generation.
     */
    public static class BulkTradeGenerator {
        private final GeneratorConfig config;

        BulkTradeGenerator(GeneratorConfig config) {
            this.config = config;
        }

        /**
         * Generate trades in parallel with optional profile corruption.
         *
         * @return List of generated trades
         */
        public List<Trade> generate() {
            TradeFactory factory = new TradeFactory(config.getTradeTypeDistribution());

            return IntStream.range(0, config.getCount())
                    .parallel()
                    .mapToObj(i -> {
                        Trade trade = factory.createGenerator().generate();

                        // Apply data profile corruption if configured
                        if (config.isUseProfiles()) {
                            DataProfile profile = selectProfile(config.getProfileDistribution());
                            trade = ProfileApplier.apply(trade, profile);
                        }

                        return trade;
                    })
                    .collect(Collectors.toList());
        }

        /**
         * Select a data profile based on weighted distribution.
         */
        private DataProfile selectProfile(Map<DataProfile, Integer> distribution) {
            int random = ThreadLocalRandom.current().nextInt(100);
            int cumulative = 0;

            for (Map.Entry<DataProfile, Integer> entry : distribution.entrySet()) {
                cumulative += entry.getValue();
                if (random < cumulative) {
                    return entry.getKey();
                }
            }

            return DataProfile.CLEAN; // Fallback
        }
    }
}