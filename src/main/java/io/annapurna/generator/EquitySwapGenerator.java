package io.annapurna.generator;

import net.datafaker.Faker;
import io.annapurna.model.Currency;
import io.annapurna.model.EquitySwap;
import io.annapurna.model.Trade;
import io.annapurna.model.TradeType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class EquitySwapGenerator implements TradeGenerator {

    private final Random random;
    private final boolean useThreadLocal;
    private final Faker faker;

    private static final String[] REFERENCE_ASSETS = {
            "AAPL", "AAPL", "AAPL",
            "SPX", "SPX", "SPX", "SPX",
            "MSFT", "MSFT",
            "GOOGL", "GOOGL",
            "TSLA",
            "AMZN",
            "META",
            "NVDA", "NVDA"
    };

    private static final String[] TIER1_BANKS = {
            "JP Morgan", "Goldman Sachs", "Morgan Stanley", "Bank of America",
            "Citigroup", "Barclays", "Deutsche Bank", "UBS"
    };

    private static final String[] TIER2_BANKS = {
            "Wells Fargo", "BNP Paribas", "HSBC",
            "Societe Generale", "RBC", "TD Securities"
    };

    private static final String[] SETTLEMENT_FREQUENCIES = { "MONTHLY", "MONTHLY", "QUARTERLY" };
    private static final String[] RETURN_TYPES = { "TOTAL_RETURN", "PRICE_RETURN" };
    private static final int[] TENORS = { 3, 6, 12, 24 };

    // Default constructor: Production mode (ThreadLocalRandom)
    public EquitySwapGenerator() {
        this.random = null;
        this.useThreadLocal = true;
        this.faker = new Faker();
    }

    // Seeded constructor: Test mode (deterministic)
    public EquitySwapGenerator(long seed) {
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

        BigDecimal targetNotional = generateNotional();
        String referenceAsset = selectReferenceAsset();
        BigDecimal initialPrice = generateInitialPrice(referenceAsset);
        BigDecimal quantity = targetNotional.divide(initialPrice, 0, RoundingMode.HALF_UP);
        BigDecimal finalNotional = quantity.multiply(initialPrice);

        String counterparty = selectCounterparty(finalNotional);
        int tenorMonths = TENORS[nextInt(TENORS.length)];
        Integer fundingSpreadBps = generateFundingSpread(finalNotional);

        return EquitySwap.builder()
                .tradeId(generateTradeId(tradeDate))
                .tradeDate(tradeDate)
                .settlementDate(tradeDate.plusDays(2))
                .maturityDate(tradeDate.plusMonths(tenorMonths))
                .notional(finalNotional)
                .currency(Currency.USD)
                .counterparty(counterparty)
                .book(generateBook(referenceAsset))
                .trader(generateTrader())
                .referenceAsset(referenceAsset)
                .returnType(RETURN_TYPES[nextInt(RETURN_TYPES.length)])
                .fundingLeg(generateFundingLeg(fundingSpreadBps))
                .fundingSpreadBps(fundingSpreadBps)
                .settlementFrequency(SETTLEMENT_FREQUENCIES[nextInt(SETTLEMENT_FREQUENCIES.length)])
                .initialPrice(initialPrice)
                .quantity(quantity)
                .direction(nextBoolean() ? "LONG" : "SHORT")
                .build();
    }

    @Override
    public TradeType getTradeType() {
        return TradeType.EQUITY_SWAP;
    }

    private String generateTradeId(LocalDate tradeDate) {
        String date = tradeDate.toString().replace("-", "");
        int sequence = nextInt(100000);
        return String.format("ANNAPURNA-EQS-%s-%05d", date, sequence);
    }

    private LocalDate generateTradeDate() {
        LocalDate date = LocalDate.now().minusDays(nextInt(365));
        while (date.getDayOfWeek().getValue() >= 6) {
            date = date.minusDays(1);
        }
        return date;
    }

    private BigDecimal generateNotional() {
        double logMin = Math.log(5_000_000);
        double logMax = Math.log(100_000_000);
        double logValue = logMin + (logMax - logMin) * nextDouble();
        double notional = Math.exp(logValue);
        long millions = Math.round(notional / 1_000_000);
        return BigDecimal.valueOf(millions * 1_000_000);
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

    private String selectReferenceAsset() {
        return REFERENCE_ASSETS[nextInt(REFERENCE_ASSETS.length)];
    }

    private String generateFundingLeg(Integer spreadBps) {
        String index = nextDouble() < 0.9 ? "SOFR" : "FED_FUNDS";
        return String.format("%s + %dbps", index, spreadBps);
    }

    private Integer generateFundingSpread(BigDecimal notional) {
        int baseSpread = 100;
        if (notional.compareTo(BigDecimal.valueOf(50_000_000)) > 0) {
            baseSpread -= 30;
        } else if (notional.compareTo(BigDecimal.valueOf(20_000_000)) > 0) {
            baseSpread -= 15;
        }
        int variance = nextInt(61) - 30;
        int spread = baseSpread + variance;
        return Math.max(50, Math.min(200, spread));
    }

    private BigDecimal generateInitialPrice(String referenceAsset) {
        double price;
        if ("SPX".equals(referenceAsset)) {
            price = 4000 + nextDouble() * 1000;
        } else {
            price = 100 + nextDouble() * 300;
        }
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }

    private String generateBook(String referenceAsset) {
        if (isUSStock(referenceAsset)) {
            String[] usBooks = { "EQ_DERIV_NY", "EQUITY_SWAPS_US" };
            return usBooks[nextInt(usBooks.length)];
        }
        if ("SPX".equals(referenceAsset)) {
            String[] globalBooks = { "EQ_DERIV_NY", "EQ_DERIV_LON", "EQ_DERIV_HK" };
            return globalBooks[nextInt(globalBooks.length)];
        }
        return "EQ_DERIV_NY";
    }

    private boolean isUSStock(String referenceAsset) {
        return !referenceAsset.equals("SPX") && !referenceAsset.equals("NDX");
    }

    private String generateTrader() {
        return faker.name().firstName() + " " + faker.name().lastName();
    }
}