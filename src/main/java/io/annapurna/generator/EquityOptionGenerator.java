package io.annapurna.generator;

import net.datafaker.Faker;
import io.annapurna.model.Currency;
import io.annapurna.model.EquityOption;
import io.annapurna.model.Trade;
import io.annapurna.model.TradeType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class EquityOptionGenerator implements TradeGenerator {

    private final Random random;
    private final boolean useThreadLocal;
    private final Faker faker;

    private static final String[] TIER1_BANKS = {
            "JP Morgan", "Goldman Sachs", "Morgan Stanley", "Bank of America",
            "Citigroup", "Barclays", "Deutsche Bank", "UBS"
    };

    private static final String[] TIER2_BANKS = {
            "Wells Fargo", "BNP Paribas", "HSBC",
            "Societe Generale", "RBC", "TD Securities"
    };

    private static final String[] UNDERLYING_ASSETS = {
            "SPX", "SPX", "SPX", "SPX", "SPX",
            "AAPL", "AAPL", "AAPL",
            "TSLA", "TSLA", "TSLA",
            "MSFT", "MSFT",
            "NVDA", "NVDA",
            "AMZN", "GOOGL", "META", "NDX"
    };

    private static final String[] OPTION_TYPES = { "CALL", "PUT" };

    // Contract size distribution
    private static final int[] CONTRACT_SIZES = { 1, 1, 5, 5, 10, 10, 25, 50, 100, 200 };

    private static final ExpiryConfig[] EXPIRY_CONFIGS = {
            new ExpiryConfig("WEEKLY", 7, 20),
            new ExpiryConfig("MONTHLY", 30, 50),
            new ExpiryConfig("QUARTERLY", 90, 20),
            new ExpiryConfig("LEAPS", 365, 10)
    };

    // Default constructor: Production mode (ThreadLocalRandom)
    public EquityOptionGenerator() {
        this.random = null;
        this.useThreadLocal = true;
        this.faker = new Faker();
    }

    // Seeded constructor: Test mode (deterministic)
    public EquityOptionGenerator(long seed) {
        this.random = new Random(seed);
        this.useThreadLocal = false;
        this.faker = new Faker(new Random(seed));
    }

    // Helper methods for random generation
    private int nextInt(int bound) {
        return useThreadLocal ?
                ThreadLocalRandom.current().nextInt(bound) :
                random.nextInt(bound);
    }

    private double nextDouble() {
        return useThreadLocal ?
                ThreadLocalRandom.current().nextDouble() :
                random.nextDouble();
    }

    private boolean nextBoolean() {
        return useThreadLocal ?
                ThreadLocalRandom.current().nextBoolean() :
                random.nextBoolean();
    }

    @Override
    public Trade generate() {
        LocalDate tradeDate = generateTradeDate();
        String underlying = selectUnderlying();

        // 1. Price Discovery
        BigDecimal spotPrice = generateSpotPrice(underlying);
        BigDecimal impliedVol = generateImpliedVolatility(underlying);

        // 2. Structuring
        ExpiryConfig expiryConfig = selectExpiry();
        LocalDate expiryDate = calculateExpiryDate(tradeDate, expiryConfig);
        String moneyness = selectMoneyness();
        String optionType = OPTION_TYPES[nextInt(OPTION_TYPES.length)];

        BigDecimal strikePrice = calculateStrike(spotPrice, moneyness, optionType);

        // 3. Pricing
        int daysToExpiry = (int) java.time.temporal.ChronoUnit.DAYS.between(tradeDate, expiryDate);
        BigDecimal premium = calculatePremium(spotPrice, strikePrice, daysToExpiry, optionType, impliedVol);

        // 4. Sizing
        int contracts = CONTRACT_SIZES[nextInt(CONTRACT_SIZES.length)];
        BigDecimal quantity = BigDecimal.valueOf(contracts * 100); // 100 shares per contract

        // Effective Notional = Spot Price * Total Shares (Risk Exposure)
        BigDecimal notional = spotPrice.multiply(quantity);

        String counterparty = selectCounterparty(notional);

        return EquityOption.builder()
                .tradeId(generateTradeId(tradeDate))
                .tradeDate(tradeDate)
                .settlementDate(tradeDate.plusDays(1)) // Options settle T+1
                .maturityDate(expiryDate)
                .notional(notional)
                .currency(Currency.USD)
                .counterparty(counterparty)
                .book(generateBook(underlying)) // Book matches Asset Region
                .trader(generateTrader())
                .underlyingAsset(underlying)
                .optionType(optionType)
                .strikePrice(strikePrice)
                .spotPrice(spotPrice)
                .premium(premium)
                .contracts(contracts)
                .quantity(quantity)
                .expiryDate(expiryDate)
                .exerciseStyle(selectExerciseStyle(underlying))
                .moneyness(moneyness)
                .position(nextBoolean() ? "LONG" : "SHORT")
                .impliedVolatility(impliedVol)
                .build();
    }

    @Override
    public TradeType getTradeType() {
        return TradeType.EQUITY_OPTION;
    }

    private String generateTradeId(LocalDate tradeDate) {
        String date = tradeDate.toString().replace("-", "");
        int sequence = nextInt(100000);
        return String.format("ANNAPURNA-OPT-%s-%05d", date, sequence);
    }

    private LocalDate generateTradeDate() {
        LocalDate date = LocalDate.now().minusDays(nextInt(365));
        while (date.getDayOfWeek().getValue() >= 6) {
            date = date.minusDays(1);
        }
        return date;
    }

    private String selectUnderlying() {
        return UNDERLYING_ASSETS[nextInt(UNDERLYING_ASSETS.length)];
    }

    private BigDecimal generateSpotPrice(String underlying) {
        double price;
        if ("SPX".equals(underlying)) price = 4000 + nextDouble() * 1000;
        else if ("NDX".equals(underlying)) price = 15000 + nextDouble() * 2000;
        else price = 100 + nextDouble() * 300;
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }

    private ExpiryConfig selectExpiry() {
        int totalWeight = 0;
        for (ExpiryConfig c : EXPIRY_CONFIGS) totalWeight += c.weight;
        int r = nextInt(totalWeight);
        int current = 0;
        for (ExpiryConfig c : EXPIRY_CONFIGS) {
            current += c.weight;
            if (r < current) return c;
        }
        return EXPIRY_CONFIGS[1];
    }

    private LocalDate calculateExpiryDate(LocalDate tradeDate, ExpiryConfig config) {
        LocalDate expiry = tradeDate.plusDays(config.daysToExpiry);
        // Move to next Friday
        while (expiry.getDayOfWeek().getValue() != 5) {
            expiry = expiry.plusDays(1);
        }
        return expiry;
    }

    private String selectMoneyness() {
        double rand = nextDouble();
        if (rand < 0.40) return "ATM";
        else if (rand < 0.80) return "OTM";
        else return "ITM";
    }

    private BigDecimal calculateStrike(BigDecimal spotPrice, String moneyness, String optionType) {
        double spot = spotPrice.doubleValue();
        double strike;

        switch (moneyness) {
            case "ATM":
                strike = spot * (0.98 + nextDouble() * 0.04);
                break;
            case "OTM":
                if ("CALL".equals(optionType)) strike = spot * (1.02 + nextDouble() * 0.13); // Call > Spot
                else strike = spot * (0.85 + nextDouble() * 0.13); // Put < Spot
                break;
            case "ITM":
                if ("CALL".equals(optionType)) strike = spot * (0.90 + nextDouble() * 0.08); // Call < Spot
                else strike = spot * (1.02 + nextDouble() * 0.08); // Put > Spot
                break;
            default: strike = spot;
        }

        // Smart Rounding
        boolean isIndex = spotPrice.compareTo(BigDecimal.valueOf(1000)) > 0;
        double increment = isIndex ? 25.0 : 5.0; // Index strikes usually $25 steps, stocks $5 or $1
        strike = Math.round(strike / increment) * increment;

        return BigDecimal.valueOf(strike).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePremium(BigDecimal spotPrice, BigDecimal strikePrice,
                                        int daysToExpiry, String optionType, BigDecimal vol) {
        double spot = spotPrice.doubleValue();
        double strike = strikePrice.doubleValue();
        double volatility = vol.doubleValue() / 100.0;

        // 1. Intrinsic Value
        double intrinsic = ("CALL".equals(optionType)) ?
                Math.max(0, spot - strike) : Math.max(0, strike - spot);

        // 2. Time Value (Proxy for Black-Scholes)
        double timeValue = (spot * 0.4) * volatility * Math.sqrt(daysToExpiry / 365.0);

        // At The Money options have highest time value, OTM/ITM have less
        double moneynessFactor = 1.0 - (Math.abs(spot - strike) / spot);
        timeValue *= Math.max(0.1, moneynessFactor);

        // Add randomness via helper
        double premium = (intrinsic + timeValue) * (0.9 + nextDouble() * 0.2);

        return BigDecimal.valueOf(Math.max(0.05, premium)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal generateImpliedVolatility(String underlying) {
        double baseVol;
        if ("TSLA".equals(underlying)) baseVol = 45 + nextDouble() * 30;
        else if ("SPX".equals(underlying)) baseVol = 12 + nextDouble() * 15;
        else baseVol = 20 + nextDouble() * 25;
        return BigDecimal.valueOf(baseVol).setScale(2, RoundingMode.HALF_UP);
    }

    private String selectExerciseStyle(String underlying) {
        if ("SPX".equals(underlying) || "NDX".equals(underlying)) return "EUROPEAN";
        return "AMERICAN";
    }

    private String selectCounterparty(BigDecimal notional) {
        if (notional.compareTo(BigDecimal.valueOf(10_000_000)) > 0) {
            return TIER1_BANKS[nextInt(TIER1_BANKS.length)];
        } else {
            return nextBoolean() ?
                    TIER1_BANKS[nextInt(TIER1_BANKS.length)] :
                    TIER2_BANKS[nextInt(TIER2_BANKS.length)];
        }
    }

    private String generateBook(String underlying) {
        String[] usBooks = { "EQ_OPTIONS_NY", "EQ_OPTIONS_NY", "EQ_OPTIONS_US" };

        if ("SPX".equals(underlying) || "NDX".equals(underlying)) {
            String[] globalBooks = { "EQ_OPTIONS_NY", "EQ_OPTIONS_LON" };
            return globalBooks[nextInt(globalBooks.length)];
        }

        return usBooks[nextInt(usBooks.length)];
    }

    private String generateTrader() {
        return faker.name().firstName() + " " + faker.name().lastName();
    }

    private static class ExpiryConfig {
        final String type;
        final int daysToExpiry;
        final int weight;
        ExpiryConfig(String type, int daysToExpiry, int weight) {
            this.type = type; this.daysToExpiry = daysToExpiry; this.weight = weight;
        }
    }
}