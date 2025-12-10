package io.annapurna.generator;

import net.datafaker.Faker;
import io.annapurna.model.Currency;
import io.annapurna.model.InterestRateSwap;
import io.annapurna.model.Trade;
import io.annapurna.model.TradeType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class InterestRateSwapGenerator implements TradeGenerator {

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

    private static final int[] TENORS_YEARS = { 2, 2, 5, 5, 5, 10, 10, 10, 30, 30 };
    private static final String[] FLOATING_INDICES = { "SOFR", "SOFR", "SOFR", "SOFR", "EURIBOR" };

    // Default constructor: Production mode (ThreadLocalRandom)
    public InterestRateSwapGenerator() {
        this.random = null;
        this.useThreadLocal = true;
        this.faker = new Faker();
    }

    // Seeded constructor: Test mode (deterministic)
    public InterestRateSwapGenerator(long seed) {
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
        int tenorYears = TENORS_YEARS[nextInt(TENORS_YEARS.length)];

        BigDecimal notional = generateNotional();
        String counterparty = selectCounterparty(notional);

        // 1. Determine Index & Currency First (Drivers)
        String floatingIndex = FLOATING_INDICES[nextInt(FLOATING_INDICES.length)];
        Currency currency = selectCurrency(floatingIndex);

        // 2. Generate Rates
        BigDecimal fixedRate = generateFixedRate(tenorYears);
        Integer floatingSpreadBps = generateFloatingSpread();

        return InterestRateSwap.builder()
                .tradeId(generateTradeId(tradeDate))
                .tradeDate(tradeDate)
                .settlementDate(tradeDate.plusDays(2))
                .effectiveDate(tradeDate.plusDays(2))
                .maturityDate(tradeDate.plusYears(tenorYears))
                .notional(notional)
                .currency(currency)
                .counterparty(counterparty)
                .book(generateBook(currency)) // Book depends on Currency
                .trader(generateTrader())
                .fixedRate(fixedRate)
                .floatingRateIndex(floatingIndex)
                .floatingSpreadBps(floatingSpreadBps)
                .fixedLegFrequency(generateFixedFrequency(currency)) // Standard per currency
                .floatingLegFrequency("QUARTERLY") // Standard for almost all modern swaps
                .dayCountConvention(generateDayCount(currency)) // Standard per currency
                .direction(nextBoolean() ? "PAY_FIXED" : "RECEIVE_FIXED")
                .build();
    }

    @Override
    public TradeType getTradeType() {
        return TradeType.INTEREST_RATE_SWAP;
    }

    private String generateTradeId(LocalDate tradeDate) {
        String date = tradeDate.toString().replace("-", "");
        int sequence = nextInt(100000);
        return String.format("ANNAPURNA-IRS-%s-%05d", date, sequence);
    }

    private LocalDate generateTradeDate() {
        LocalDate date = LocalDate.now().minusDays(nextInt(365));
        while (date.getDayOfWeek().getValue() >= 6) {
            date = date.minusDays(1);
        }
        return date;
    }

    private BigDecimal generateNotional() {
        double logMin = Math.log(10_000_000);
        double logMax = Math.log(500_000_000);
        double logValue = logMin + (logMax - logMin) * nextDouble();
        double notional = Math.exp(logValue);
        long millions = Math.round(notional / 1_000_000);
        return BigDecimal.valueOf(millions * 1_000_000);
    }

    private String selectCounterparty(BigDecimal notional) {
        if (notional.compareTo(BigDecimal.valueOf(100_000_000)) > 0) {
            return TIER1_BANKS[nextInt(TIER1_BANKS.length)];
        } else {
            return nextBoolean() ?
                    TIER1_BANKS[nextInt(TIER1_BANKS.length)] :
                    TIER2_BANKS[nextInt(TIER2_BANKS.length)];
        }
    }

    private BigDecimal generateFixedRate(int tenorYears) {
        double baseRate;
        switch (tenorYears) {
            case 2: baseRate = 2.75 + nextDouble() * 0.75; break;
            case 5: baseRate = 3.25 + nextDouble() * 0.75; break;
            case 10: baseRate = 3.75 + nextDouble() * 0.75; break;
            case 30: baseRate = 4.50 + nextDouble() * 1.00; break;
            default: baseRate = 3.50;
        }
        return BigDecimal.valueOf(baseRate).setScale(3, RoundingMode.HALF_UP);
    }

    private Integer generateFloatingSpread() {
        double rand = nextDouble();
        if (rand < 0.60) return 0;
        else if (rand < 0.85) return 10 + nextInt(21);
        else return 30 + nextInt(21);
    }

    private Currency selectCurrency(String floatingIndex) {
        if ("EURIBOR".equals(floatingIndex)) return Currency.EUR;
        return Currency.USD;
    }

    // Select Booking region based on Currency
    private String generateBook(Currency currency) {
        if (currency == Currency.USD) {
            String[] usBooks = { "RATES_NY", "RATES_NY", "RATES_DERIV_US" };
            return usBooks[nextInt(usBooks.length)];
        } else {
            // EUR trades go to London or EMEA
            String[] euBooks = { "RATES_LON", "RATES_DERIV_EMEA", "RATES_DERIV_EMEA" };
            return euBooks[nextInt(euBooks.length)];
        }
    }

    // Select Day Count based on Market Standards
    private String generateDayCount(Currency currency) {
        if (currency == Currency.USD) {
            // US Standard: ACT/360 for Float is dominant
            return "ACT/360";
        } else {
            // Euro Standard
            return "30/360";
        }
    }

    // Select Frequency based on Market Standards
    private String generateFixedFrequency(Currency currency) {
        if (currency == Currency.USD) {
            return "SEMI_ANNUAL"; // Standard US Swap
        } else {
            return "ANNUAL"; // Standard Euro Swap
        }
    }

    private String generateTrader() {
        return faker.name().firstName() + " " + faker.name().lastName();
    }
}