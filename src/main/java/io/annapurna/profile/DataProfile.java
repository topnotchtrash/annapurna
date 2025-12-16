package io.annapurna.profile;

/**
 * Data quality profile for generated trades.
 *
 * Defines three levels of data quality for testing different scenarios:
 * - CLEAN: Production-quality data
 * - EDGE_CASE: Valid but unusual data for boundary testing
 * - STRESS: Invalid data for error handling testing
 */
public enum DataProfile {

    /**
     * Clean data - 100% valid, realistic trades.
     *
     * All required fields present, all values within normal ranges.
     * Business logic is correct (e.g., notional = quantity * price).
     * Dates are valid and in correct sequence.
     *
     * Use this for: Production testing, demos, realistic datasets
     */
    CLEAN,

    /**
     * Edge case data - Valid but unusual.
     *
     * Examples:
     * - Missing optional fields (counterparty, trader, book)
     * - Extreme but valid notionals ($1 or $10B)
     * - Exotic/illiquid assets or currency pairs
     * - Boundary dates (weekends, near holidays)
     * - Unusual but valid combinations
     *
     * Use this for: Boundary testing, robustness checks
     */
    EDGE_CASE,

    /**
     * Stress data - Invalid/broken for testing error handling.
     *
     * Examples:
     * - Missing required fields (currency, tradeDate, tradeId)
     * - Invalid dates (settlement before trade, maturity in past)
     * - Null values in critical fields
     * - Negative or zero notionals
     * - Broken business logic (quantity * price != notional)
     * - Future trade dates
     *
     * Use this for: Error handling testing, validation testing
     */
    STRESS
}