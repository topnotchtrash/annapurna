package io.annapurna;

import io.annapurna.model.Trade;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Performance benchmarks for Annapurna trade generation.
 *
 * Tests generation speed under different configurations:
 * - Clean data only
 * - Mixed profiles (with corruption)
 * - Different trade type distributions
 * - Large datasets
 */
class PerformanceBenchmarkTest {

    @Test
    void benchmarkCleanDataGeneration() {
        System.out.println("\n=== Benchmark: Clean Data Generation ===");

        // Warm-up
        Annapurna.generate(1000);

        // Test different sizes
        int[] sizes = {1_000, 10_000, 50_000, 100_000};

        for (int size : sizes) {
            long start = System.currentTimeMillis();
            List<Trade> trades = Annapurna.generate(size);
            long duration = System.currentTimeMillis() - start;

            assertEquals(size, trades.size());

            int rate = (int) (size * 1000L / duration);
            System.out.printf("%,7d trades in %,5d ms = %,7d trades/sec%n",
                    size, duration, rate);
        }
    }

    @Test
    void benchmarkMixedProfileGeneration() {
        System.out.println("\n=== Benchmark: Mixed Profile Generation (70/20/10) ===");

        // Warm-up
        Annapurna.builder()
                .tradeTypes()
                .equitySwap(100)
                .dataProfile()
                .clean(70)
                .edgeCase(20)
                .stress(10)
                .count(1000)
                .build()
                .generate();

        // Test different sizes
        int[] sizes = {1_000, 10_000, 50_000, 100_000};

        for (int size : sizes) {
            long start = System.currentTimeMillis();
            List<Trade> trades = Annapurna.builder()
                    .tradeTypes()
                    .equitySwap(100)
                    .dataProfile()
                    .clean(70)
                    .edgeCase(20)
                    .stress(10)
                    .count(size)
                    .build()
                    .generate();
            long duration = System.currentTimeMillis() - start;

            assertEquals(size, trades.size());

            int rate = (int) (size * 1000L / duration);
            System.out.printf("%,7d trades in %,5d ms = %,7d trades/sec%n",
                    size, duration, rate);
        }
    }

    @Test
    void benchmarkStressProfileGeneration() {
        System.out.println("\n=== Benchmark: 100% Stress Profile ===");

        // Warm-up
        Annapurna.builder()
                .tradeTypes()
                .equitySwap(100)
                .dataProfile()
                .stress(100)
                .count(1000)
                .build()
                .generate();

        // Test different sizes
        int[] sizes = {1_000, 10_000, 50_000};

        for (int size : sizes) {
            long start = System.currentTimeMillis();
            List<Trade> trades = Annapurna.builder()
                    .tradeTypes()
                    .equitySwap(100)
                    .dataProfile()
                    .stress(100)
                    .count(size)
                    .build()
                    .generate();
            long duration = System.currentTimeMillis() - start;

            assertEquals(size, trades.size());

            int rate = (int) (size * 1000L / duration);
            System.out.printf("%,7d trades in %,5d ms = %,7d trades/sec%n",
                    size, duration, rate);
        }
    }

    @Test
    void benchmarkAllTradeTypes() {
        System.out.println("\n=== Benchmark: All 5 Trade Types (Equal Distribution) ===");

        // Warm-up
        Annapurna.generate(1000);

        // Test with equal distribution
        int[] sizes = {1_000, 10_000, 50_000};

        for (int size : sizes) {
            long start = System.currentTimeMillis();
            List<Trade> trades = Annapurna.builder()
                    .tradeTypes()
                    .equitySwap(20)
                    .interestRateSwap(20)
                    .fxForward(20)
                    .option(20)
                    .cds(20)
                    .count(size)
                    .build()
                    .generate();
            long duration = System.currentTimeMillis() - start;

            assertEquals(size, trades.size());

            int rate = (int) (size * 1000L / duration);
            System.out.printf("%,7d trades in %,5d ms = %,7d trades/sec%n",
                    size, duration, rate);
        }
    }

    @Test
    void benchmarkComplexScenario() {
        System.out.println("\n=== Benchmark: Complex Scenario (All Types + Mixed Profiles) ===");

        // Warm-up
        Annapurna.builder()
                .tradeTypes()
                .equitySwap(30)
                .interestRateSwap(30)
                .fxForward(20)
                .option(15)
                .cds(5)
                .dataProfile()
                .clean(70)
                .edgeCase(20)
                .stress(10)
                .count(1000)
                .build()
                .generate();

        // Test realistic production scenario
        int[] sizes = {1_000, 10_000, 50_000, 100_000};

        for (int size : sizes) {
            long start = System.currentTimeMillis();
            List<Trade> trades = Annapurna.builder()
                    .tradeTypes()
                    .equitySwap(30)
                    .interestRateSwap(30)
                    .fxForward(20)
                    .option(15)
                    .cds(5)
                    .dataProfile()
                    .clean(70)
                    .edgeCase(20)
                    .stress(10)
                    .count(size)
                    .build()
                    .generate();
            long duration = System.currentTimeMillis() - start;

            assertEquals(size, trades.size());

            int rate = (int) (size * 1000L / duration);
            System.out.printf("%,7d trades in %,5d ms = %,7d trades/sec%n",
                    size, duration, rate);
        }
    }

    @Test
    void compareCleanVsMixedProfile() {
        System.out.println("\n=== Comparison: Clean vs Mixed Profile (10K trades) ===");

        int count = 10_000;

        // Test 1: Clean only
        long startClean = System.currentTimeMillis();
        List<Trade> cleanTrades = Annapurna.generate(count);
        long cleanDuration = System.currentTimeMillis() - startClean;

        // Test 2: Mixed profile
        long startMixed = System.currentTimeMillis();
        List<Trade> mixedTrades = Annapurna.builder()
                .tradeTypes()
                .equitySwap(100)
                .dataProfile()
                .clean(70)
                .edgeCase(20)
                .stress(10)
                .count(count)
                .build()
                .generate();
        long mixedDuration = System.currentTimeMillis() - startMixed;

        assertEquals(count, cleanTrades.size());
        assertEquals(count, mixedTrades.size());

        int cleanRate = (int) (count * 1000L / cleanDuration);
        int mixedRate = (int) (count * 1000L / mixedDuration);

        System.out.printf("Clean profile:  %,5d ms = %,7d trades/sec%n", cleanDuration, cleanRate);
        System.out.printf("Mixed profile:  %,5d ms = %,7d trades/sec%n", mixedDuration, mixedRate);

        double overhead = ((double) mixedDuration / cleanDuration - 1) * 100;
        System.out.printf("Profile overhead: %.1f%%%n", overhead);
    }

    @Test
    void benchmarkParallelism() {
        System.out.println("\n=== Benchmark: Parallelism Impact (50K trades) ===");

        int count = 50_000;
        int[] threadCounts = {1, 2, 4, 8, 16};

        for (int threads : threadCounts) {
            long start = System.currentTimeMillis();
            List<Trade> trades = Annapurna.builder()
                    .count(count)
                    .parallelism(threads)
                    .build()
                    .generate();
            long duration = System.currentTimeMillis() - start;

            assertEquals(count, trades.size());

            int rate = (int) (count * 1000L / duration);
            System.out.printf("%2d threads: %,5d ms = %,7d trades/sec%n",
                    threads, duration, rate);
        }
    }
}