# Annapurna

High-performance synthetic financial trade data generator for Java. Generates realistic trade data across 5 asset classes with configurable data quality profiles.

## Features

- 5 trade types: Equity Swaps, Interest Rate Swaps, FX Forwards, Equity Options, Credit Default Swaps
- Performance: 680K+ trades/second with parallel generation
- Data quality profiles: Clean, edge-case, and stress-test data
- Thread-safe parallel execution using ThreadLocalRandom
- Realistic financial domain logic and business conventions
- JSON and CSV export support

## Installation

```xml
<dependency>
    <groupId>io.github.topnotchtrash</groupId>
    <artifactId>annapurna</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Requirements

- Java 17 or higher
- Maven 3.6+

## Basic Usage

### Generate One Trade

```java
Trade trade = Annapurna.generateOne();
```

### Generate Multiple Trades

```java
List<Trade> trades = Annapurna.generate(10000);
```

### Custom Configuration

```java
List<Trade> trades = Annapurna.builder()
    .tradeTypes()
        .equitySwap(30)
        .interestRateSwap(30)
        .fxForward(20)
        .option(15)
        .cds(5)
    .count(100000)
    .parallelism(8)
    .build()
    .generate();
```

## Data Quality Profiles

Generate intentionally corrupted data for testing error handling:

```java
List<Trade> trades = Annapurna.builder()
    .tradeTypes()
        .equitySwap(100)
    .dataProfile()
        .clean(70)      // 70% perfect data
        .edgeCase(20)   // 20% edge cases (missing optional fields, extreme values)
        .stress(10)     // 10% broken data (null required fields, invalid dates)
    .count(10000)
    .build()
    .generate();
```

### Profile Types

- **CLEAN**: All fields valid, realistic values, correct business logic
- **EDGE_CASE**: Valid but unusual (missing optional fields, extreme notionals, boundary values)
- **STRESS**: Invalid data (null required fields, negative notionals, broken date sequences)

## Trade Types

### Equity Swap

Swap returns on an equity position for a funding leg (typically SOFR + spread).

- Reference assets: AAPL, SPX, MSFT, GOOGL, NVDA, TSLA, AMZN, META
- Notional range: $5M - $100M
- Tenors: 3M (20%), 6M (30%), 1Y (40%), 2Y (10%)
- Funding: SOFR + spread (50-200 bps)
- Return types: Total Return (70%), Price Return (30%)
- Settlement: T+2, Monthly (60%) or Quarterly (40%)

**Key Features:**
- Notional = Quantity × Price (mathematically correct)
- Spreads inversely correlated with notional size
- Counterparty selection based on deal size
- Regional book routing (US stocks → US books)

### Interest Rate Swap

Exchange fixed rate payments for floating rate payments.

- Currencies: USD (SOFR), EUR (EURIBOR)
- Notional range: $10M - $500M
- Tenors: 2Y (10%), 5Y (30%), 10Y (30%), 30Y (20%)
- Fixed rates: 2.5% - 5.5% (yield curve-based)
- Floating spreads: 0-50 bps (60% have zero spread)
- Payment frequencies: Semi-annual (fixed), Quarterly (floating)
- Day count conventions: ACT/360, 30/360, ACT/365

**Key Features:**
- Fixed rates increase with tenor (normal yield curve)
- Plain vanilla structure (minimal spreads)
- Currency-matched floating indices

### FX Forward

Contract to exchange currencies at a future date at a predetermined rate.

- Currency pairs: EUR/USD (30%), USD/JPY (20%), GBP/USD (15%), plus 5 others
- Notional range: $500K - $200M
- Tenors: 1M (30%), 3M (35%), 6M (20%), 1Y (15%)
- Settlement types: Physical (80%), Non-Deliverable (20%)

**Key Features:**
- Forward points based on interest rate differentials
- Realistic spot rate ranges per currency pair
- Proper pip precision (4 decimals for most pairs, 2 for JPY)

### Equity Option

Right (but not obligation) to buy or sell equity at strike price.

- Underlyings: SPX (30%), AAPL (15%), TSLA (15%), MSFT, NVDA, AMZN, GOOGL, META, NDX
- Strike distribution: 40% ATM, 40% OTM, 20% ITM
- Option types: Call, Put
- Expiry patterns: Weekly (20%), Monthly (50%), Quarterly (20%), LEAPS (10%)
- Contract sizes: 1-200 contracts (100 shares per contract)
- Exercise styles: American (stocks), European (indices)

**Key Features:**
- Strike prices rounded to $5 (stocks) or $25 (indices)
- Premium based on intrinsic + time value
- Implied volatility varies by underlying (TSLA: 50-80%, SPX: 15-30%)
- Expiry on Fridays

### Credit Default Swap

Insurance against credit default on a reference entity.

- Reference entities:
    - Investment Grade: Apple (AAA, 15-25 bps), Microsoft (AAA), Walmart (AA), Coca-Cola (A)
    - High Yield: Ford (BB, 250-350 bps), American Airlines (B, 400-600 bps), Hertz (CCC, 800-1200 bps)
    - Sovereigns: Italy (BBB), Brazil (BB), Argentina (CC, 1500-3000 bps)
- Notional range: $5M - $100M
- Tenors: 1Y (10%), 3Y (20%), 5Y (50%), 10Y (20%)
- Payment frequency: Quarterly
- Recovery rates: 60-70% (senior secured), 40% (senior unsecured), 20-30% (subordinated)
- Upfront payments: 0% (IG), 0-5% (HY), 15-40% (distressed)

**Key Features:**
- Spreads correlated with credit rating
- Sector-specific recovery assumptions
- Restructuring clauses: CR, MM, XR

## Serialization

### JSON Export

```java
List<Trade> trades = Annapurna.generate(1000);

JsonSerializer serializer = new JsonSerializer();
serializer.write(trades, "trades.json");
```

Output format:
```json
[
  {
    "tradeType": "EQUITY_SWAP",
    "tradeId": "ANNAPURNA-EQS-20251204-79575",
    "tradeDate": "2025-12-04",
    "settlementDate": "2025-12-06",
    "maturityDate": "2026-12-04",
    "notional": 45000163.65,
    "currency": "USD",
    "counterparty": "Barclays",
    "referenceAsset": "NVDA",
    "returnType": "TOTAL_RETURN",
    "fundingLeg": "SOFR + 84bps",
    ...
  }
]
```

### CSV Export

```java
List<Trade> trades = Annapurna.generate(1000);

CsvSerializer serializer = new CsvSerializer();
serializer.writeByType(trades, "trades");
```

This creates separate CSV files for each trade type:
- `trades_equity_swaps.csv`
- `trades_interest_rate_swaps.csv`
- `trades_fx_forwards.csv`
- `trades_equity_options.csv`
- `trades_cds.csv`

Each CSV has appropriate headers and properly escaped values.

## Performance Benchmarks

All benchmarks run on 8-core CPU with parallel generation enabled.

### Generation Performance

| Trades    | Time    | Rate (trades/sec) | Configuration              |
|-----------|---------|-------------------|----------------------------|
| 1,000     | 26 ms   | 38,461            | Clean data only            |
| 10,000    | 122 ms  | 81,967            | Clean data only            |
| 50,000    | 257 ms  | 194,552           | Clean data only            |
| 100,000   | 286 ms  | 349,650           | Clean data only            |
| 10,000    | 25 ms   | 400,000           | Mixed profiles (70/20/10)  |
| 50,000    | 101 ms  | 495,049           | Mixed profiles (70/20/10)  |
| 100,000   | 147 ms  | 680,272           | Mixed profiles (70/20/10)  |
| 10,000    | 14 ms   | 714,285           | All 5 trade types          |
| 50,000    | 55 ms   | 909,090           | All 5 trade types          |

**Peak Performance:** 909,090 trades/second (50K trades, all 5 types)

### Profile Overhead

| Configuration       | Time    | Rate (trades/sec) |
|---------------------|---------|-------------------|
| Clean profile only  | 43 ms   | 232,558           |
| Mixed profile       | 25 ms   | 400,000           |
| Overhead            | -41.9%  | Negative overhead |

**Note:** Mixed profiles are faster than clean because corruption logic is simpler than some complex generation logic.

### Parallelism Impact (50K trades)

| Threads | Time    | Rate (trades/sec) |
|---------|---------|-------------------|
| 1       | 121 ms  | 413,223           |
| 2       | 74 ms   | 675,675           |
| 4       | 85 ms   | 588,235           |
| 8       | 66 ms   | 757,575           |
| 16      | 65 ms   | 769,230           |

**Optimal:** 8-16 threads (diminishing returns beyond 8 cores)

### Serialization Performance (10K trades)

| Format | Time    | File Size |
|--------|---------|-----------|
| JSON   | 215 ms  | 5.6 MB    |
| CSV    | 124 ms  | ~5 MB     |

## Architecture

### Thread Safety

- Uses `ThreadLocalRandom` for lock-free parallel execution
- Each generator instance can be shared across threads
- No synchronized blocks or contention
- Seeded constructors available for deterministic testing

### Design Patterns

- **Strategy Pattern**: TradeGenerator interface with 5 implementations
- **Builder Pattern**: Fluent API for configuration
- **Factory Pattern**: TradeFactory for weighted generator selection
- **Template Method**: Trade abstract base class

### Package Structure

```
io.annapurna
├── Annapurna.java              // Main API entry point
├── AnnapurnaBuilder.java       // Fluent configuration API
├── config/
│   └── GeneratorConfig.java    // Configuration storage
├── generator/
│   ├── TradeGenerator.java     // Interface
│   ├── TradeFactory.java       // Factory for weighted selection
│   ├── EquitySwapGenerator.java
│   ├── InterestRateSwapGenerator.java
│   ├── FXForwardGenerator.java
│   ├── EquityOptionGenerator.java
│   └── CDSGenerator.java
├── model/
│   ├── Trade.java              // Abstract base
│   ├── TradeType.java          // Enum
│   ├── Currency.java           // Enum
│   ├── EquitySwap.java
│   ├── InterestRateSwap.java
│   ├── FXForward.java
│   ├── EquityOption.java
│   └── CreditDefaultSwap.java
├── profile/
│   ├── DataProfile.java        // Enum (CLEAN, EDGE_CASE, STRESS)
│   ├── CorruptionStrategy.java // Interface
│   └── ProfileApplier.java     // Corruption logic
└── serialization/
    ├── JsonSerializer.java
    └── CsvSerializer.java
```

## Use Cases

### Testing
- Generate large datasets for performance testing
- Create edge cases for validation testing
- Generate broken data for error handling tests
- Test data pipelines with realistic trade structures

### Development
- Mock data for UI development
- Populate test databases
- Load testing for downstream systems
- Integration testing

### Training
- Machine learning model training data
- Trading system simulations
- Risk model calibration
- Market data analysis

## Realistic Features

### Financial Accuracy

- **Equity Swaps**: Notional = Quantity × Price (mathematically correct)
- **Interest Rate Swaps**: Yield curve-based fixed rates
- **FX Forwards**: Forward points from interest rate differentials
- **Options**: Premium = Intrinsic Value + Time Value
- **CDS**: Spreads correlated with credit ratings

### Market Conventions

- T+2 settlement for equities and FX spot
- T+1 settlement for options and CDS
- Quarterly payments for CDS (market standard)
- Semi-annual fixed leg for IRS (market standard)
- Friday expiry for equity options

### Business Logic

- Counterparty selection based on notional size (large deals → Tier 1 banks)
- Book routing based on asset region (US stocks → US books)
- Funding spreads inversely correlated with notional
- Recovery rates based on seniority
- Proper date sequencing (trade < settlement < maturity)

## Limitations

### What's Not Included

- Holiday calendars (weekends are skipped, but not holidays)
- Market data validation (prices are random within realistic ranges)
- Exotic products (barrier options, exotic swaps, structured products)
- Full pricing models (simplified premium/spread calculations)
- Real-time market data integration

### Future Enhancements

- Business day calculators with holiday support
- More sophisticated pricing models
- Additional trade types (commodities, convertibles)
- Streaming API for large datasets
- Configurable date ranges per trade

## Building from Source

```bash
# Clone repository
git clone https://github.com/YOUR_USERNAME/annapurna.git
cd annapurna

# Build
mvn clean install

# Run tests
mvn test

# Skip GPG signing for local builds
mvn clean install -Dgpg.skip=true
```

## License

MIT License - see LICENSE file for details

## Contributing

Contributions welcome. Please:
1. Fork the repository
2. Create a feature branch
3. Add tests for new features
4. Ensure all tests pass
5. Submit a pull request

## Support

For issues and questions:
- GitHub Issues: https://github.com/topnotchtrash/annapurna/issues
- Documentation: https://github.com/topnotchtrash/annapurna/wiki

## Version History

### 1.0.0 (Current)
- Initial release
- 5 trade types
- Data quality profiles
- JSON/CSV serialization
- 680K+ trades/second performance