package io.annapurna.config;

import io.annapurna.model.TradeType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for trade generation.
 */
public class GeneratorConfig {

    private final int count;
    private final Map<TradeType, Integer> tradeTypeDistribution;
    private final int parallelism;
    private final LocalDate startDate;
    private final LocalDate endDate;

    private GeneratorConfig(Builder builder) {
        this.count = builder.count;
        this.tradeTypeDistribution = new HashMap<>(builder.tradeTypeDistribution);
        this.parallelism = builder.parallelism;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
    }

    public int getCount() {
        return count;
    }

    public Map<TradeType, Integer> getTradeTypeDistribution() {
        return new HashMap<>(tradeTypeDistribution);
    }

    public int getParallelism() {
        return parallelism;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public static class Builder {
        private int count = 1000;
        private Map<TradeType, Integer> tradeTypeDistribution = new HashMap<>();
        private int parallelism = Runtime.getRuntime().availableProcessors();
        private LocalDate startDate = LocalDate.now().minusYears(1);
        private LocalDate endDate = LocalDate.now();

        public Builder() {
            // Default distribution: equal weight
            setDefaultDistribution();
        }

        private void setDefaultDistribution() {
            tradeTypeDistribution.put(TradeType.EQUITY_SWAP, 20);
            tradeTypeDistribution.put(TradeType.INTEREST_RATE_SWAP, 20);
            tradeTypeDistribution.put(TradeType.FX_FORWARD, 20);
            tradeTypeDistribution.put(TradeType.EQUITY_OPTION, 20);
            tradeTypeDistribution.put(TradeType.CREDIT_DEFAULT_SWAP, 20);
        }

        public Builder count(int count) {
            if (count <= 0) {
                throw new IllegalArgumentException("Count must be positive");
            }
            this.count = count;
            return this;
        }

        public Builder tradeTypeDistribution(Map<TradeType, Integer> distribution) {
            this.tradeTypeDistribution = new HashMap<>(distribution);
            return this;
        }

        public Builder parallelism(int parallelism) {
            if (parallelism <= 0) {
                throw new IllegalArgumentException("Parallelism must be positive");
            }
            this.parallelism = parallelism;
            return this;
        }

        public Builder dateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
            return this;
        }

        public GeneratorConfig build() {
            // Validate distribution sums to 100
            int total = tradeTypeDistribution.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            if (total != 100) {
                throw new IllegalArgumentException(
                        "Trade type percentages must sum to 100, got: " + total
                );
            }

            return new GeneratorConfig(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}