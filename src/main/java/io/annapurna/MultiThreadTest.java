package io.annapurna;

import io.annapurna.model.Trade;
import io.annapurna.model.TradeType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MultiThreadTest {
    public static void main(String[] args) {
        System.out.println("=== Test 1: Generate 10 trades ===");
        List<Trade> trades = Annapurna.generate(10);
        trades.forEach(System.out::println);

        System.out.println("\n=== Test 2: Custom distribution ===");
        List<Trade> customTrades = Annapurna.builder()
                .tradeTypes()
                .equitySwap(40)
                .interestRateSwap(30)
                .fxForward(20)
                .option(5)
                .cds(5)
                .count(100)
                .parallelism(4)
                .build()
                .generate();

        // Count distribution
        Map<TradeType, Long> dist = customTrades.stream()
                .collect(Collectors.groupingBy(Trade::getTradeType, Collectors.counting()));

        System.out.println("Distribution: " + dist);

        System.out.println("\n=== Test 3: Performance ===");
        long start = System.currentTimeMillis();
        List<Trade> largeBatch = Annapurna.generate(10000);
        long duration = System.currentTimeMillis() - start;

        System.out.println("Generated " + largeBatch.size() + " trades in " + duration + "ms");
        System.out.println("Rate: " + (largeBatch.size() * 1000 / duration) + " trades/second");
    }
}
