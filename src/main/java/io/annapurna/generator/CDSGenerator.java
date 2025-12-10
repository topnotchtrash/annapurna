package io.annapurna.generator;

import net.datafaker.Faker;
import io.annapurna.model.CreditDefaultSwap;
import io.annapurna.model.Currency;
import io.annapurna.model.Trade;
import io.annapurna.model.TradeType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Random;

public class CDSGenerator implements TradeGenerator {

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

    private static final ReferenceEntityConfig[] REFERENCE_ENTITIES = {
            // Investment Grade (IG)
            new ReferenceEntityConfig("Apple Inc", "AAPL", "TECHNOLOGY", "AAA", 15, 25, 15),
            new ReferenceEntityConfig("Microsoft Corp", "MSFT", "TECHNOLOGY", "AAA", 15, 25, 15),
            new ReferenceEntityConfig("Walmart Inc", "WMT", "RETAIL", "AA", 35, 55, 10),
            new ReferenceEntityConfig("Coca-Cola Co", "KO", "CONSUMER", "A", 50, 80, 10),

            // High Yield (HY)
            new ReferenceEntityConfig("Ford Motor Co", "F", "AUTOMOTIVE", "BB", 250, 350, 10),
            new ReferenceEntityConfig("American Airlines", "AAL", "AIRLINES", "B", 400, 600, 10),
            new ReferenceEntityConfig("Hertz Global", "HTZ", "AUTOMOTIVE", "CCC", 800, 1200, 5),

            // Sovereign
            new ReferenceEntityConfig("Republic of Italy", "ITALY", "SOVEREIGN", "BBB", 120, 160, 5),
            new ReferenceEntityConfig("Republic of Brazil", "BRAZIL", "SOVEREIGN", "BB", 220, 300, 5),
            new ReferenceEntityConfig("Republic of Argentina", "ARG", "SOVEREIGN", "CC", 1500, 3000, 5)
    };

    // Standard CDS Tenors
    private static final int[] TENORS_YEARS = { 1, 3, 5, 5, 5, 5, 10 }; // Heavily weighted to 5Y

    // Restructuring Clauses
    private static final String[] RESTRUCTURING_CLAUSES = { "CR", "MM", "XR" };

    public CDSGenerator() {
        this.random = new Random();
        this.faker = new Faker();
    }

    public CDSGenerator(long seed) {
        this.random = new Random(seed);
        this.faker = new Faker(new Random(seed));
    }

    @Override
    public Trade generate() {
        LocalDate tradeDate = generateTradeDate();
        int tenorYears = TENORS_YEARS[random.nextInt(TENORS_YEARS.length)];

        // 1. Select Entity & Counterparty
        ReferenceEntityConfig entity = selectReferenceEntity();
        BigDecimal notional = generateNotional();
        String counterparty = selectCounterparty(notional);

        // 2. Determine Seniority (90% Senior Unsecured)
        String seniority = generateSeniority(entity.sector);

        // 3. Generate Spread & Recovery
        Integer spreadBps = generateSpread(entity);
        BigDecimal recoveryRate = generateRecoveryRate(seniority, entity.sector);

        // 4. Calculate Upfront (For distressed names)
        BigDecimal upfrontPayment = generateUpfrontPayment(entity.creditRating, notional);

        return CreditDefaultSwap.builder()
                .tradeId(generateTradeId(tradeDate))
                .tradeDate(tradeDate)
                .settlementDate(tradeDate.plusDays(1)) // T+1 is standard for CDS today
                .maturityDate(tradeDate.plusYears(tenorYears)) // Note: Real CDS roll on Mar/Jun/Sep/Dec 20th
                .notional(notional)
                .currency(Currency.USD)
                .counterparty(counterparty)
                .book(generateBook(entity)) // FIX: Book depends on Rating/Sector
                .trader(generateTrader())
                .referenceEntity(entity.name)
                .referenceTicker(entity.ticker)
                .sector(entity.sector)
                .creditRating(entity.creditRating)
                .spreadBps(spreadBps)
                .upfrontPayment(upfrontPayment)
                .recoveryRate(recoveryRate)
                .paymentFrequency("QUARTERLY")
                .position(random.nextBoolean() ? "PROTECTION_BUYER" : "PROTECTION_SELLER")
                .restructuringClause(RESTRUCTURING_CLAUSES[random.nextInt(RESTRUCTURING_CLAUSES.length)])
                .seniority(seniority)
                .build();
    }

    @Override
    public TradeType getTradeType() {
        return TradeType.CREDIT_DEFAULT_SWAP;
    }

    private String generateTradeId(LocalDate tradeDate) {
        String date = tradeDate.toString().replace("-", "");
        int sequence = random.nextInt(100000);
        return String.format("ANNAPURNA-CDS-%s-%05d", date, sequence);
    }

    private LocalDate generateTradeDate() {
        LocalDate date = LocalDate.now().minusDays(random.nextInt(365));
        while (date.getDayOfWeek().getValue() >= 6) date = date.minusDays(1);
        return date;
    }

    private BigDecimal generateNotional() {
        double logMin = Math.log(5_000_000);
        double logMax = Math.log(100_000_000);
        double logValue = logMin + (logMax - logMin) * random.nextDouble();
        long millions = Math.round(Math.exp(logValue) / 1_000_000);
        return BigDecimal.valueOf(millions * 1_000_000);
    }

    private String selectCounterparty(BigDecimal notional) {
        if (notional.compareTo(BigDecimal.valueOf(50_000_000)) > 0) return TIER1_BANKS[random.nextInt(TIER1_BANKS.length)];
        return random.nextBoolean() ? TIER1_BANKS[random.nextInt(TIER1_BANKS.length)] : TIER2_BANKS[random.nextInt(TIER2_BANKS.length)];
    }

    private ReferenceEntityConfig selectReferenceEntity() {
        int totalWeight = 0;
        for (ReferenceEntityConfig e : REFERENCE_ENTITIES) totalWeight += e.weight;
        int r = random.nextInt(totalWeight);
        int current = 0;
        for (ReferenceEntityConfig e : REFERENCE_ENTITIES) {
            current += e.weight;
            if (r < current) return e;
        }
        return REFERENCE_ENTITIES[0];
    }

    private Integer generateSpread(ReferenceEntityConfig entity) {
        int range = entity.spreadMax - entity.spreadMin;
        return entity.spreadMin + random.nextInt(range + 1);
    }

    private BigDecimal generateUpfrontPayment(String creditRating, BigDecimal notional) {
        double upfrontPercent = 0;
        // Distressed names require massive upfront points
        if (creditRating.startsWith("C")) upfrontPercent = 0.15 + random.nextDouble() * 0.25; // 15-40%
        else if (creditRating.equals("B")) {
            if (random.nextDouble() < 0.2) upfrontPercent = 0.01 + random.nextDouble() * 0.04; // Occasional points
        }
        return notional.multiply(BigDecimal.valueOf(upfrontPercent)).setScale(2, RoundingMode.HALF_UP);
    }

    // FIX: Seniority Logic
    private String generateSeniority(String sector) {
        if ("SOVEREIGN".equals(sector)) return "SENIOR_SECURED"; // Sovereigns technically different, but usually top tier
        // Corporates
        double r = random.nextDouble();
        if (r < 0.90) return "SENIOR_UNSECURED"; // Standard Reference Obligation
        if (r < 0.95) return "SENIOR_SECURED";
        return "SUBORDINATED";
    }

    private BigDecimal generateRecoveryRate(String seniority, String sector) {
        if ("SOVEREIGN".equals(sector)) return BigDecimal.valueOf(25 + random.nextInt(15)); // Sovereigns ~25-40%

        switch (seniority) {
            case "SENIOR_SECURED": return BigDecimal.valueOf(60 + random.nextInt(10));
            case "SENIOR_UNSECURED": return BigDecimal.valueOf(40); // Market Standard
            case "SUBORDINATED": return BigDecimal.valueOf(20 + random.nextInt(10));
            default: return BigDecimal.valueOf(40);
        }
    }

    // FIX: Book Logic based on Rating
    private String generateBook(ReferenceEntityConfig entity) {
        if ("SOVEREIGN".equals(entity.sector)) return "CREDIT_EM"; // Emerging Markets

        String r = entity.creditRating;
        if (r.startsWith("A") || r.startsWith("BBB")) {
            return "CREDIT_IG_NY"; // Investment Grade
        } else {
            return "CREDIT_HY_NY"; // High Yield
        }
    }

    private String generateTrader() {
        return faker.name().firstName() + " " + faker.name().lastName();
    }

    private static class ReferenceEntityConfig {
        final String name, ticker, sector, creditRating;
        final int spreadMin, spreadMax, weight;
        ReferenceEntityConfig(String n, String t, String s, String c, int min, int max, int w) {
            name=n; ticker=t; sector=s; creditRating=c; spreadMin=min; spreadMax=max; weight=w;
        }
    }
}