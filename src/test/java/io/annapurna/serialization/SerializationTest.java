package io.annapurna.serialization;

import io.annapurna.Annapurna;
import io.annapurna.model.Trade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SerializationTest {

    @TempDir
    Path tempDir;

    @Test
    void testJsonSerializeSingleTrade() throws IOException {
        // Generate a single trade
        Trade trade = Annapurna.generateOne();

        // Serialize to JSON
        JsonSerializer serializer = new JsonSerializer();
        File outputFile = tempDir.resolve("trade.json").toFile();
        serializer.write(trade, outputFile.getAbsolutePath());

        // Verify file exists and has content
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 100); // Should have substantial content

        // Read back and verify
        String json = Files.readString(outputFile.toPath());
        assertTrue(json.contains("tradeId"));
        assertTrue(json.contains("tradeDate"));
        assertTrue(json.contains("notional"));

        System.out.println("JSON output preview:");
        System.out.println(json.substring(0, Math.min(500, json.length())));
    }

    @Test
    void testJsonSerializeMultipleTrades() throws IOException {
        // Generate 100 trades
        List<Trade> trades = Annapurna.generate(100);

        // Serialize to JSON
        JsonSerializer serializer = new JsonSerializer();
        File outputFile = tempDir.resolve("trades.json").toFile();
        serializer.write(trades, outputFile.getAbsolutePath());

        // Verify file exists
        assertTrue(outputFile.exists());
        long fileSize = outputFile.length();
        System.out.println("Generated JSON file size: " + fileSize + " bytes");

        // Should be substantial (100 trades)
        assertTrue(fileSize > 10000, "File too small: " + fileSize);

        // Verify it's valid JSON array
        String json = Files.readString(outputFile.toPath());
        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
    }

    @Test
    void testJsonToString() throws IOException {
        Trade trade = Annapurna.generateOne();

        JsonSerializer serializer = new JsonSerializer();
        String json = serializer.toJson(trade);

        assertNotNull(json);
        assertTrue(json.length() > 100);
        assertTrue(json.contains("tradeId"));

        System.out.println("JSON string length: " + json.length());
    }

    @Test
    void testCsvSerializeEquitySwaps() throws IOException {
        // Generate only equity swaps
        List<Trade> trades = Annapurna.builder()
                .tradeTypes()
                .equitySwap(100)
                .count(50)
                .build()
                .generate();

        // Serialize to CSV
        CsvSerializer serializer = new CsvSerializer();
        File outputFile = tempDir.resolve("equity_swaps.csv").toFile();
        serializer.write(trades, outputFile.getAbsolutePath());

        // Verify file exists
        assertTrue(outputFile.exists());

        // Read and verify structure
        List<String> lines = Files.readAllLines(outputFile.toPath());

        // Should have header + 50 data rows
        assertEquals(51, lines.size(), "Expected 51 lines (header + 50 trades)");

        // Verify header
        String header = lines.get(0);
        assertTrue(header.contains("tradeId"));
        assertTrue(header.contains("referenceAsset"));
        assertTrue(header.contains("fundingLeg"));

        // Verify first data row has content
        String firstRow = lines.get(1);
        String[] fields = firstRow.split(",");
        assertTrue(fields.length >= 15, "Expected at least 15 fields");

        System.out.println("CSV Header: " + header);
        System.out.println("First row: " + firstRow);
        System.out.println("Total lines: " + lines.size());
    }

    @Test
    void testCsvSerializeByType() throws IOException {
        // Generate mixed trades
        List<Trade> trades = Annapurna.builder()
                .tradeTypes()
                .equitySwap(40)
                .interestRateSwap(30)
                .fxForward(30)
                .count(100)
                .build()
                .generate();

        // Serialize by type
        CsvSerializer serializer = new CsvSerializer();
        String baseFilepath = tempDir.resolve("trades").toFile().getAbsolutePath();
        serializer.writeByType(trades, baseFilepath);

        // Verify files were created
        File equitySwapsFile = new File(baseFilepath + "_equity_swaps.csv");
        File irsFile = new File(baseFilepath + "_interest_rate_swaps.csv");
        File fxFile = new File(baseFilepath + "_fx_forwards.csv");

        assertTrue(equitySwapsFile.exists(), "Equity swaps file not created");
        assertTrue(irsFile.exists(), "IRS file not created");
        assertTrue(fxFile.exists(), "FX forwards file not created");

        // Verify each file has reasonable size
        assertTrue(equitySwapsFile.length() > 1000);
        assertTrue(irsFile.length() > 1000);
        assertTrue(fxFile.length() > 1000);

        System.out.println("Equity Swaps file: " + equitySwapsFile.length() + " bytes");
        System.out.println("IRS file: " + irsFile.length() + " bytes");
        System.out.println("FX Forwards file: " + fxFile.length() + " bytes");
    }

    @Test
    void testCsvHandlesNullValues() throws IOException {
        // Generate trades with edge cases (some nulls)
        List<Trade> trades = Annapurna.builder()
                .tradeTypes()
                .equitySwap(100)
                .dataProfile()
                .edgeCase(100)  // Will have some null fields
                .count(20)
                .build()
                .generate();

        // Serialize to CSV
        CsvSerializer serializer = new CsvSerializer();
        File outputFile = tempDir.resolve("edge_cases.csv").toFile();
        serializer.write(trades, outputFile.getAbsolutePath());

        // Should handle nulls gracefully
        assertTrue(outputFile.exists());
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(21, lines.size()); // header + 20 rows

        // Check that rows don't have "null" strings
        String firstDataRow = lines.get(1);
        // Empty fields should be empty, not "null"
        assertFalse(firstDataRow.contains("null,null"), "Should not have 'null' strings");

        System.out.println("Edge case CSV created successfully");
    }

    @Test
    void testLargeDatasetPerformance() throws IOException {
        System.out.println("\n=== Large Dataset Test ===");

        // Generate 10,000 trades
        long startGen = System.currentTimeMillis();
        List<Trade> trades = Annapurna.generate(10000);
        long genTime = System.currentTimeMillis() - startGen;

        System.out.println("Generated 10,000 trades in " + genTime + "ms");
        System.out.println("Rate: " + (10000 * 1000 / genTime) + " trades/second");

        // Serialize to JSON
        long startJson = System.currentTimeMillis();
        JsonSerializer jsonSerializer = new JsonSerializer();
        File jsonFile = tempDir.resolve("large_dataset.json").toFile();
        jsonSerializer.write(trades, jsonFile.getAbsolutePath());
        long jsonTime = System.currentTimeMillis() - startJson;

        System.out.println("JSON serialization: " + jsonTime + "ms");
        System.out.println("JSON file size: " + (jsonFile.length() / 1024) + " KB");

        // Serialize to CSV (by type)
        long startCsv = System.currentTimeMillis();
        CsvSerializer csvSerializer = new CsvSerializer();
        String basePath = tempDir.resolve("large_dataset").toFile().getAbsolutePath();
        csvSerializer.writeByType(trades, basePath);
        long csvTime = System.currentTimeMillis() - startCsv;

        System.out.println("CSV serialization: " + csvTime + "ms");

        // Verify files exist
        assertTrue(jsonFile.exists());
        assertTrue(jsonFile.length() > 100000); // Should be > 100KB
    }
}