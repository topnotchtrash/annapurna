package io.annapurna.generator;

import io.annapurna.model.Trade;

/**
 * Strategy interface for generating different types of trades.
 * Each trade type will have its own generator implementation.
 */
public interface TradeGenerator {

    /**
     * Generate a single trade with realistic data.
     *
     * @return A fully populated Trade object
     */
    Trade generate();

    /**
     * Get the trade type this generator produces.
     *
     * @return The TradeType enum value
     */
    io.annapurna.model.TradeType getTradeType();
}