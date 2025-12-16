package io.annapurna.config;

import io.annapurna.model.TradeType;
import io.annapurna.profile.DataProfile;

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
    private final Map<DataProfile, Integer> profileDistribution;  // FIXED: Removed static
    private final boolean useProfiles;  // FIXED: Removed static

    private GeneratorConfig(Builder builder) {
        this.count = builder.count;
        this.tradeTypeDistribution = new HashMap<>(builder.tradeTypeDistribution);
        this.parallelism = builder.parallelism;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.profileDistribution = new HashMap<>(builder.profileDistribution);
        this.useProfiles = builder.useProfiles;
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

    public Map<DataProfile, Integer> getProfileDistribution() {
        return new HashMap<>(profileDistribution);
    }

    public boolean isUseProfiles() {
        return useProfiles;
    }

    public static class Builder {
        private int count = 1000;
        private Map<TradeType, Integer> tradeTypeDistribution = new HashMap<>();
        private int parallelism = Runtime.getRuntime().availableProcessors();
        private LocalDate startDate = LocalDate.now().minusYears(1);
        private LocalDate endDate = LocalDate.now();
        private Map<DataProfile, Integer> profileDistribution = new HashMap<>();  // ADDED
        private boolean useProfiles = false;  // ADDED

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

        public Builder profileDistribution(Map<DataProfile, Integer> distribution) {
            this.profileDistribution = new HashMap<>(distribution);
            this.useProfiles = true;
            return this;
        }

        public Builder useProfiles(boolean useProfiles) {
            this.useProfiles = useProfiles;
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

            // Validate profile distribution if profiles are used
            if (useProfiles && !profileDistribution.isEmpty()) {
                int profileTotal = profileDistribution.values().stream()
                        .mapToInt(Integer::intValue)
                        .sum();

                if (profileTotal != 100) {
                    throw new IllegalArgumentException(
                            "Data profile percentages must sum to 100, got: " + profileTotal
                    );
                }
            }

            return new GeneratorConfig(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}