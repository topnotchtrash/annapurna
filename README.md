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
- Reference assets: AAPL, SPX, MSFT, GOOGL, NVDA, TSLA, AMZN, META
- Notional range: $5M - $100M
- Tenors: 3M, 6M, 1Y, 2Y
- Funding: SOFR + spread (50-200 bps)
- Return types: Total Return, Price Return

### Interest Rate Swap
- Currencies: USD (SOFR), EUR (EURIBOR)
- Notional range: $10M - $500M
- Tenors: 2Y, 5Y, 10Y, 30Y
- Fixed rates: 2.5% - 5.5% (yield curve-based)
- Payment frequencies: Semi-annual (fixed), Quarterly (floating)

### FX Forward
- Currency pairs: EUR/USD, USD/JPY, GBP/USD, USD/CHF, AUD/USD, EUR/GBP, EUR/JPY, GBP/JPY
- Notional range: $500K - $200M
- Tenors: 1M, 3M, 6M, 1Y
- Forward points calculated from interest rate differentials
- Settlement types: Physical, Non-Deliverable

### Equity Option
- Underlyings: SPX, AAPL, TSLA, MSFT, NVDA, AMZN, GOOGL, META, NDX
- Strike distribution: 40% ATM, 40% OTM, 20% ITM
- Expiry patterns: Weekly (20%), Monthly (50%), Quarterly (20%), LEAPS (10%)
- Contract sizes: 1-200 contracts
- Exercise styles: American (stocks), European (indices)

### Credit Default Swap
- Reference entities: Investment