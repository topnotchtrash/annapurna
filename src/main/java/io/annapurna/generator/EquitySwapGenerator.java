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

public class EquitySwapGenerator implements TradeGenerator {

    //  Use java.util.Random for the field to allow seeding safely
    private final Random random;
    private final Faker faker;

    private static final String[] REFERENCE_ASSETS = {
            "AAPL", "AAPL", "AAPL",
            "SPX", "SPX", "SPX", "SPX",
            "MSFT", "MSFT",
            "GOOGL", "GOOGL",
            "TSLA",
            "AMZN",
            "META",
            "NVDA", "NVDA" // Increased NVDA weight due to recent market cap growth
    };

    private static final String[] TIER1_BANKS = {
            "JP Morgan", "Goldman Sachs", "Morgan Stanley", "Bank of America",
            "Citigroup", "Barclays", "Deutsche Bank", "UBS"
    };

    private static final String[] TIER2_BANKS = {
            "Wells Fargo", "BNP Paribas", "Credit Suisse", "HSBC",
            "Societe Generale", "RBC", "TD Securities"
    };

    private static final String[] SETTLEMENT_FREQUENCIES = { "MONTHLY", "MONTHLY", "QUARTERLY" };
    private static final String[] RETURN_TYPES = { "TOTAL_RETURN", "PRICE_RETURN" };
    private static final int[] TENORS = { 3, 6, 12, 24 };

    // Default constructor uses a random seed
    public EquitySwapGenerator() {
        this.random = new Random();
        this.faker = new Faker();
    }

    // Seeded constructor for reproducibility
    public EquitySwapGenerator(long seed) {
        this.random = new Random(seed);
        this.faker = new Faker(new Random(seed));
    }

    @Override
    public Trade generate() {
        LocalDate tradeDate = generateTradeDate();

        // 1. Generate Target Notional
        BigDecimal targetNotional = generateNotional();

        // 2. Select Asset & Price
        String referenceAsset = selectReferenceAsset();
        BigDecimal initialPrice = generateInitialPrice(referenceAsset);

        // 3. Calculate Quantity (Round to nearest whole share)
        BigDecimal quantity = targetNotional.divide(initialPrice, 0, RoundingMode.HALF_UP);


        BigDecimal finalNotional = quantity.multiply(initialPrice);

        String counterparty = selectCounterparty(finalNotional);
        int tenorMonths = TENORS[random.nextInt(TENORS.length)];
        Integer fundingSpreadBps = generateFundingSpread(finalNotional);

        return EquitySwap.builder()
                .tradeId(generateTradeId(tradeDate))
                .tradeDate(tradeDate)
                .settlementDate(tradeDate.plusDays(2))
                .maturityDate(tradeDate.plusMonths(tenorMonths))
                .notional(finalNotional) // Use the recalculated math-perfect notional
                .currency(Currency.USD)
                .counterparty(counterparty)
                .book(generateBook(referenceAsset))
                .trader(generateTrader())
                .referenceAsset(referenceAsset)
                .returnType(RETURN_TYPES[random.nextInt(RETURN_TYPES.length)])
                .fundingLeg(generateFundingLeg(fundingSpreadBps))
                .fundingSpreadBps(fundingSpreadBps)
                .settlementFrequency(SETTLEMENT_FREQUENCIES[random.nextInt(SETTLEMENT_FREQUENCIES.length)])
                .initialPrice(initialPrice)
                .quantity(quantity)
                .direction(random.nextBoolean() ? "LONG" : "SHORT")
                .build();
    }

    @Override
    public TradeType getTradeType() {
        return TradeType.EQUITY_SWAP;
    }

    private String generateTradeId(LocalDate tradeDate) {
        String date = tradeDate.toString().replace("-", "");
        int sequence = random.nextInt(100000);
        return String.format("ANNAPURNA-EQS-%s-%05d", date, sequence);
    }

    private LocalDate generateTradeDate() {
        LocalDate date = LocalDate.now().minusDays(random.nextInt(365));
        while (date.getDayOfWeek().getValue() >= 6) {
            date = date.minusDays(1);
        }
        return date;
    }

    private BigDecimal generateNotional() {
        double logMin = Math.log(5_000_000);
        double logMax = Math.log(100_000_000);
        double logValue = logMin + (logMax - logMin) * random.nextDouble();
        double notional = Math.exp(logValue);
        long millions = Math.round(notional / 1_000_000);
        return BigDecimal.valueOf(millions * 1_000_000);
    }

    private String selectCounterparty(BigDecimal notional) {
        if (notional.compareTo(BigDecimal.valueOf(50_000_000)) > 0) {
            return TIER1_BANKS[random.nextInt(TIER1_BANKS.length)];
        } else {
            return random.nextBoolean() ?
                    TIER1_BANKS[random.nextInt(TIER1_BANKS.length)] :
                    TIER2_BANKS[random.nextInt(TIER2_BANKS.length)];
        }
    }

    private String selectReferenceAsset() {
        return REFERENCE_ASSETS[random.nextInt(REFERENCE_ASSETS.length)];
    }

    private String generateFundingLeg(Integer spreadBps) {
        //  Removed LIBOR, replaced with FED_FUNDS for variety, or just SOFR
        String index = random.nextDouble() < 0.9 ? "SOFR" : "FED_FUNDS";
        return String.format("%s + %dbps", index, spreadBps);
    }

    private Integer generateFundingSpread(BigDecimal notional) {
        int baseSpread = 100;
        if (notional.compareTo(BigDecimal.valueOf(50_000_000)) > 0) {
            baseSpread -= 30;
        } else if (notional.compareTo(BigDecimal.valueOf(20_000_000)) > 0) {
            baseSpread -= 15;
        }
        int variance = random.nextInt(61) - 30;
        int spread = baseSpread + variance;
        return Math.max(50, Math.min(200, spread));
    }

    private BigDecimal generateInitialPrice(String referenceAsset) {
        double price;
        if ("SPX".equals(referenceAsset)) {
            price = 4000 + random.nextDouble() * 1000;
        } else {
            price = 100 + random.nextDouble() * 300;
        }
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }

    private String generateBook(String referenceAsset) {
        if (isUSStock(referenceAsset)) {
            String[] usBooks = { "EQ_DERIV_NY", "EQUITY_SWAPS_US" };
            return usBooks[random.nextInt(usBooks.length)];
        }
        if ("SPX".equals(referenceAsset)) {
            String[] globalBooks = { "EQ_DERIV_NY", "EQ_DERIV_LON", "EQ_DERIV_HK" };
            return globalBooks[random.nextInt(globalBooks.length)];
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