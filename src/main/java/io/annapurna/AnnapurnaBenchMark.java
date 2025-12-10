package io.annapurna;

import io.annapurna.generator.EquitySwapGenerator;
import io.annapurna.generator.TradeGenerator;
import io.annapurna.model.Trade;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class AnnapurnaBenchMark {

    // Generate 1 Million trades for the test
    private static final int TRADE_COUNT = 1_000_000;

    public static void main(String[] args) throws Exception {
        System.out.println("==========================================");
        System.out.println("   ANNAPURNA GENERATOR PERFORMANCE TEST   ");
        System.out.println("==========================================");
        System.out.println("Generating " + String.format("%,d", TRADE_COUNT) + " trades per test.");
        System.out.println("CPU Cores Available: " + Runtime.getRuntime().availableProcessors());
        System.out.println("------------------------------------------\n");

        // 1. Setup the Generator in PROD MODE (No Seed = ThreadLocalRandom)
        TradeGenerator generator = new EquitySwapGenerator();

        // --- TEST 1: SINGLE THREADED ---
        runSingleThreaded(generator);

        System.out.println("\n------------------------------------------\n");

        // --- TEST 2: MULTI THREADED ---
        runMultiThreaded(generator);

        System.out.println("\n==========================================");
    }

    private static void runSingleThreaded(TradeGenerator generator) {
        System.out.println(">>> Starting SINGLE-THREADED Test...");

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < TRADE_COUNT; i++) {
            Trade t = generator.generate();
            
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Completed Single-Threaded.");
        System.out.println("Time: " + duration + " ms");
        System.out.println(" Speed: " + String.format("%,d", (TRADE_COUNT * 1000L) / duration) + " trades/sec");
    }

    private static void runMultiThreaded(TradeGenerator generator) throws InterruptedException {
        System.out.println(">>> Starting MULTI-THREADED Test...");

        // Create a pool of workers matching your CPU count
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);

        // Create a list of tasks
        List<Callable<Void>> tasks = new ArrayList<>();

        // We split the work into chunks to reduce overhead
        // 1 Million trades split into 100 chunks of 10,000
        int chunkSize = 10_000;
        int chunks = TRADE_COUNT / chunkSize;

        for (int i = 0; i < chunks; i++) {
            tasks.add(() -> {
                for (int j = 0; j < chunkSize; j++) {
                    // This calls ThreadLocalRandom.current().nextInt() internally
                    // No locking! extremely fast.
                    generator.generate();
                }
                return null;
            });
        }

        long startTime = System.currentTimeMillis();

        // Run all chunks in parallel
        executor.invokeAll(tasks);

        // Shut down and wait
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Completed Multi-Threaded.");
        System.out.println("Time: " + duration + " ms");
        System.out.println("Speed: " + String.format("%,d", (TRADE_COUNT * 1000L) / duration) + " trades/sec");


    }
}