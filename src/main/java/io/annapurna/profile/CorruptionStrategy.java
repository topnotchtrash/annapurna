package io.annapurna.profile;

import io.annapurna.model.Trade;

/**
 * Strategy interface for applying data quality corruption to trades.
 *
 * Implementations define how to corrupt trades based on DataProfile.
 * This allows for extensible corruption strategies per trade type.
 */
public interface CorruptionStrategy {

    /**
     * Apply corruption to a trade based on the profile.
     *
     * @param trade The trade to corrupt
     * @param profile The corruption profile to apply (CLEAN, EDGE_CASE, STRESS)
     * @return The corrupted trade (may be same instance modified)
     */
    Trade apply(Trade trade, DataProfile profile);

    /**
     * Check if this strategy applies to the given trade type.
     *
     * @param trade The trade to check
     * @return true if this strategy can corrupt this trade type
     */
    boolean appliesTo(Trade trade);
}