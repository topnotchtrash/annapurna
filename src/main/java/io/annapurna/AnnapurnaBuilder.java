package io.annapurna;

import io.annapurna.config.GeneratorConfig;
import io.annapurna.generator.TradeFactory;
import io.annapurna.model.Trade;
import io.annapurna.model.TradeType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Fluent builder for configuring bulk trade generation.
 *
 * Example:
 * <pre>
 * List&lt;Trade&gt; trades = Annapurna.builder()
 *     .tradeTypes()
 *         .equitySwap(30)
 *         .interestRateSwap(30)
 *         .fxForward(20)
 *         .option(15)
 *         .cds(5)
 *     .count(10000)
 *     .parallelism(8)
 *     .build()
 *     .generate();
 * </pre>
 */
public class AnnapurnaBuilder {

    private int count = 1000;
    private Map<TradeType, Integer> tradeTypeDistribution = new HashMap<>();
    private int parallelism = Runtime.getRuntime().availableProcessors();
    private LocalDate startDate = LocalDate.now().minusYears(1);
    private LocalDate endDate = LocalDate.now();

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
     */
    public TradeTypeBuilder tradeTypes() {
        return new TradeTypeBuilder(this);
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
     * Set parallelism level (number of threads).
     * Default: number of CPU cores.
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
         * Generate trades in parallel.
         *
         * @return List of generated trades
         */
        public List<Trade> generate() {
            TradeFactory factory = new TradeFactory(config.getTradeTypeDistribution());

            // Generate trades in parallel using streams
            return IntStream.range(0, config.getCount())
                    .parallel()
                    .mapToObj(i -> factory.createGenerator().generate())
                    .collect(Collectors.toList());
        }
    }
}