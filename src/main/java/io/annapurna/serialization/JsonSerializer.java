package io.annapurna.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.annapurna.model.Trade;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Serializer for exporting trades to JSON format.
 *
 * Supports both single trades and lists of trades.
 * Output is formatted (pretty-printed) for readability.
 */
public class JsonSerializer {

    private final ObjectMapper mapper;

    public JsonSerializer() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Write a single trade to JSON file.
     *
     * @param trade The trade to serialize
     * @param filepath Path to output file
     * @throws IOException if write fails
     */
    public void write(Trade trade, String filepath) throws IOException {
        File file = new File(filepath);
        mapper.writeValue(file, trade);
    }

    /**
     * Write multiple trades to JSON file.
     *
     * @param trades List of trades to serialize
     * @param filepath Path to output file
     * @throws IOException if write fails
     */
    public void write(List<Trade> trades, String filepath) throws IOException {
        File file = new File(filepath);
        mapper.writeValue(file, trades);
    }

    /**
     * Convert a trade to JSON string.
     *
     * @param trade The trade to serialize
     * @return JSON string representation
     * @throws IOException if serialization fails
     */
    public String toJson(Trade trade) throws IOException {
        return mapper.writeValueAsString(trade);
    }

    /**
     * Convert multiple trades to JSON string.
     *
     * @param trades List of trades to serialize
     * @return JSON string representation
     * @throws IOException if serialization fails
     */
    public String toJson(List<Trade> trades) throws IOException {
        return mapper.writeValueAsString(trades);
    }

    /**
     * Read a trade from JSON file.
     *
     * @param filepath Path to input file
     * @param tradeClass The specific trade class to deserialize
     * @return Deserialized trade
     * @throws IOException if read fails
     */
    public <T extends Trade> T read(String filepath, Class<T> tradeClass) throws IOException {
        File file = new File(filepath);
        return mapper.readValue(file, tradeClass);
    }

    /**
     * Read multiple trades from JSON file.
     *
     * @param filepath Path to input file
     * @return List of deserialized trades
     * @throws IOException if read fails
     */
    public List<Trade> readList(String filepath) throws IOException {
        File file = new File(filepath);
        return mapper.readValue(file,
                mapper.getTypeFactory().constructCollectionType(List.class, Trade.class));
    }
}