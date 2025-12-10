package io.annapurna.generator;

import net.datafaker.Faker;
import io.annapurna.model.Currency;
import io.annapurna.model.FXForward;
import io.annapurna.model.Trade;
import io.annapurna.model.TradeType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class FXForwardGenerator implements TradeGenerator {

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

    // Configuration for supported pairs
    private static final CurrencyPairConfig[] CURRENCY_PAIRS = {
            // Major pairs (80% of market)
            new CurrencyPairConfig("EUR/USD", Currency.EUR, Currency.USD, 1.05, 1.15, 30),
            new CurrencyPairConfig("USD/JPY", Currency.USD, Currency.JPY, 140.0, 155.0, 20),
            new CurrencyPairConfig("GBP/USD", Currency.GBP, Currency.USD, 1.20, 1.30, 15),
            new CurrencyPairConfig("USD/CHF", Currency.USD, Currency.CHF, 0.85, 0.95, 10),
            new CurrencyPairConfig("AUD/USD", Currency.AUD, Currency.USD, 0.60, 0.70, 5),

            // Cross pairs (20% of market)
            new CurrencyPairConfig("EUR/GBP", Currency.EUR, Currency.GBP, 0.82, 0.88, 10),
            new CurrencyPairConfig("EUR/JPY", Currency.EUR, Currency.JPY, 150.0, 165.0, 5),
            new CurrencyPairConfig("GBP/JPY", Currency.GBP, Currency.JPY, 180.0, 195.0, 5)
    };

    private static final int[] TENORS_MONTHS = { 1, 3, 3, 3, 6, 12 };

    // Default constructor: Production mode (ThreadLocalRandom)
    public FXForwardGenerator() {
        this.random = null;
        this.useThreadLocal = true;
        this.faker = new Faker();
    }

    // Seeded constructor: Test mode (deterministic)
    public FXForwardGenerator(long seed) {
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
        int tenorMonths = TENORS_MONTHS[nextInt(TENORS_MONTHS.length)];

        // 1. Select Pair
        CurrencyPairConfig pair = selectCurrencyPair();

        // 2. Generate Rates
        BigDecimal spotRate = generateSpotRate(pair);
        BigDecimal forwardRate = generateForwardRate(spotRate, tenorMonths);
        BigDecimal forwardPoints = calculateForwardPoints(spotRate, forwardRate, pair);

        BigDecimal notional = generateNotional();
        String counterparty = selectCounterparty(notional);

        // 3. Determine Dates
        // Spot Date = T+2
        // Maturity Date = Spot Date + Tenor
        LocalDate spotDate = tradeDate.plusDays(2);
        LocalDate maturityDate = spotDate.plusMonths(tenorMonths);

        return FXForward.builder()
                .tradeId(generateTradeId(tradeDate))
                .tradeDate(tradeDate)
                // In Forwards, Settlement happens at Maturity
                .settlementDate(maturityDate)
                .maturityDate(maturityDate)
                .notional(notional)
                .currency(pair.baseCurrency)
                .counterparty(counterparty)
                .book(generateBook(pair))
                .trader(generateTrader())
                .currencyPair(pair.pairName)
                .baseCurrency(pair.baseCurrency)
                .quoteCurrency(pair.quoteCurrency)
                .spotRate(spotRate)
                .forwardRate(forwardRate)
                .forwardPoints(forwardPoints)
                .direction(nextBoolean() ? "BUY" : "SELL")
                // FIX: All these pairs are Physical. NDFs are only for restricted currencies.
                .settlementType("PHYSICAL")
                .build();
    }

    @Override
    public TradeType getTradeType() {
        return TradeType.FX_FORWARD;
    }

    private String generateTradeId(LocalDate tradeDate) {
        String date = tradeDate.toString().replace("-", "");
        int sequence = nextInt(100000);
        return String.format("ANNAPURNA-FXF-%s-%05d", date, sequence);
    }

    private LocalDate generateTradeDate() {
        LocalDate date = LocalDate.now().minusDays(nextInt(365));
        while (date.getDayOfWeek().getValue() >= 6) {
            date = date.minusDays(1);
        }
        return date;
    }

    private BigDecimal generateNotional() {
        double logMin = Math.log(500_000);
        double logMax = Math.log(200_000_000);
        double logValue = logMin + (logMax - logMin) * nextDouble();
        double notional = Math.exp(logValue);
        long hundredK = Math.round(notional / 100_000);
        return BigDecimal.valueOf(hundredK * 100_000);
    }

    private String selectCounterparty(BigDecimal notional) {
        if (notional.compareTo(BigDecimal.valueOf(50_000_000)) > 0) {
            return TIER1_BANKS[nextInt(TIER1_BANKS.length)];
        } else {
            return nextBoolean() ?
                    TIER1_BANKS[nextInt(TIER1_BANKS.length)] :
                    TIER2_BANKS[nextInt(TIER2_BANKS.length)];
        }
    }

    private CurrencyPairConfig selectCurrencyPair() {
        int totalWeight = 0;
        for (CurrencyPairConfig pair : CURRENCY_PAIRS) totalWeight += pair.weight;

        int randomWeight = nextInt(totalWeight);
        int currentWeight = 0;

        for (CurrencyPairConfig pair : CURRENCY_PAIRS) {
            currentWeight += pair.weight;
            if (randomWeight < currentWeight) return pair;
        }
        return CURRENCY_PAIRS[0];
    }

    private BigDecimal generateSpotRate(CurrencyPairConfig pair) {
        double range = pair.spotRateMax - pair.spotRateMin;
        double rate = pair.spotRateMin + (nextDouble() * range);
        int scale = isJPYPair(pair) ? 2 : 4;
        return BigDecimal.valueOf(rate).setScale(scale, RoundingMode.HALF_UP);
    }

    private BigDecimal generateForwardRate(BigDecimal spotRate, int tenorMonths) {
        // Forward points typically small (-1% to +2% annualized)
        double annualDiff = -0.01 + (nextDouble() * 0.03);
        double adjustment = spotRate.doubleValue() * annualDiff * (tenorMonths / 12.0);

        BigDecimal forwardRate = spotRate.add(BigDecimal.valueOf(adjustment));
        return forwardRate.setScale(spotRate.scale(), RoundingMode.HALF_UP);
    }

    private BigDecimal calculateForwardPoints(BigDecimal spotRate, BigDecimal forwardRate,
                                              CurrencyPairConfig pair) {
        BigDecimal diff = forwardRate.subtract(spotRate);
        // JPY Pips = 0.01, Others = 0.0001
        int pipScale = isJPYPair(pair) ? 2 : 4;
        BigDecimal multiplier = BigDecimal.valueOf(Math.pow(10, pipScale));

        return diff.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isJPYPair(CurrencyPairConfig pair) {
        return pair.baseCurrency == Currency.JPY || pair.quoteCurrency == Currency.JPY;
    }

    private String generateBook(CurrencyPairConfig pair) {
        if (pair.baseCurrency == Currency.EUR || pair.quoteCurrency == Currency.EUR) {
            return nextBoolean() ? "FX_EMEA" : "FX_LON";
        } else if (isJPYPair(pair) || pair.baseCurrency == Currency.AUD) {
            return "FX_APAC";
        } else {
            return "FX_NY";
        }
    }

    private String generateTrader() {
        return faker.name().firstName() + " " + faker.name().lastName();
    }

    private static class CurrencyPairConfig {
        final String pairName;
        final Currency baseCurrency;
        final Currency quoteCurrency;
        final double spotRateMin;
        final double spotRateMax;
        final int weight;

        CurrencyPairConfig(String pairName, Currency baseCurrency, Currency quoteCurrency,
                           double spotRateMin, double spotRateMax, int weight) {
            this.pairName = pairName;
            this.baseCurrency = baseCurrency;
            this.quoteCurrency = quoteCurrency;
            this.spotRateMin = spotRateMin;
            this.spotRateMax = spotRateMax;
            this.weight = weight;
        }
    }
}