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

public class InterestRateSwapGenerator implements TradeGenerator {

    private final Random random;
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

    public InterestRateSwapGenerator() {
        this.random = new Random();
        this.faker = new Faker();
    }

    public InterestRateSwapGenerator(long seed) {
        this.random = new Random(seed);
        this.faker = new Faker(new Random(seed));
    }

    @Override
    public Trade generate() {
        LocalDate tradeDate = generateTradeDate();
        int tenorYears = TENORS_YEARS[random.nextInt(TENORS_YEARS.length)];

        BigDecimal notional = generateNotional();
        String counterparty = selectCounterparty(notional);

        // 1. Determine Index & Currency First (Drivers)
        String floatingIndex = FLOATING_INDICES[random.nextInt(FLOATING_INDICES.length)];
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
                .book(generateBook(currency)) // FIX: Book depends on Currency
                .trader(generateTrader())
                .fixedRate(fixedRate)
                .floatingRateIndex(floatingIndex)
                .floatingSpreadBps(floatingSpreadBps)
                .fixedLegFrequency(generateFixedFrequency(currency)) // FIX: Standard per currency
                .floatingLegFrequency("QUARTERLY") // Standard for almost all modern swaps
                .dayCountConvention(generateDayCount(currency)) // FIX: Standard per currency
                .direction(random.nextBoolean() ? "PAY_FIXED" : "RECEIVE_FIXED")
                .build();
    }

    @Override
    public TradeType getTradeType() {
        return TradeType.INTEREST_RATE_SWAP;
    }

    private String generateTradeId(LocalDate tradeDate) {
        String date = tradeDate.toString().replace("-", "");
        int sequence = random.nextInt(100000);
        return String.format("ANNAPURNA-IRS-%s-%05d", date, sequence);
    }

    private LocalDate generateTradeDate() {
        LocalDate date = LocalDate.now().minusDays(random.nextInt(365));
        while (date.getDayOfWeek().getValue() >= 6) {
            date = date.minusDays(1);
        }
        return date;
    }

    private BigDecimal generateNotional() {
        double logMin = Math.log(10_000_000);
        double logMax = Math.log(500_000_000);
        double logValue = logMin + (logMax - logMin) * random.nextDouble();
        double notional = Math.exp(logValue);
        long millions = Math.round(notional / 1_000_000);
        return BigDecimal.valueOf(millions * 1_000_000);
    }

    private String selectCounterparty(BigDecimal notional) {
        if (notional.compareTo(BigDecimal.valueOf(100_000_000)) > 0) {
            return TIER1_BANKS[random.nextInt(TIER1_BANKS.length)];
        } else {
            return random.nextBoolean() ?
                    TIER1_BANKS[random.nextInt(TIER1_BANKS.length)] :
                    TIER2_BANKS[random.nextInt(TIER2_BANKS.length)];
        }
    }

    private BigDecimal generateFixedRate(int tenorYears) {
        double baseRate;
        switch (tenorYears) {
            case 2: baseRate = 2.75 + random.nextDouble() * 0.75; break;
            case 5: baseRate = 3.25 + random.nextDouble() * 0.75; break;
            case 10: baseRate = 3.75 + random.nextDouble() * 0.75; break;
            case 30: baseRate = 4.50 + random.nextDouble() * 1.00; break;
            default: baseRate = 3.50;
        }
        return BigDecimal.valueOf(baseRate).setScale(3, RoundingMode.HALF_UP);
    }

    private Integer generateFloatingSpread() {
        double rand = random.nextDouble();
        if (rand < 0.60) return 0;
        else if (rand < 0.85) return 10 + random.nextInt(21);
        else return 30 + random.nextInt(21);
    }

    private Currency selectCurrency(String floatingIndex) {
        if ("EURIBOR".equals(floatingIndex)) return Currency.EUR;
        return Currency.USD;
    }

    //  Select Booking region based on Currency
    private String generateBook(Currency currency) {
        if (currency == Currency.USD) {
            String[] usBooks = { "RATES_NY", "RATES_NY", "RATES_DERIV_US" };
            return usBooks[random.nextInt(usBooks.length)];
        } else {
            // EUR trades go to London or EMEA
            String[] euBooks = { "RATES_LON", "RATES_DERIV_EMEA", "RATES_DERIV_EMEA" };
            return euBooks[random.nextInt(euBooks.length)];
        }
    }

    //  Select Day Count based on Market Standards
    private String generateDayCount(Currency currency) {
        if (currency == Currency.USD) {
            // US Standard: 30/360 for Fixed, ACT/360 for Float.
            // Simplified: we will use ACT/360 as the dominant ISDA convention
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