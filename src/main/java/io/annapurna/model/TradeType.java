package io.annapurna.model;

/**
 * Enumeration of supported financial instrument types.
 */
public enum TradeType {
    /**
     * Interest Rate Swap - Fixed ↔ Floating interest rate swaps
     */
    INTEREST_RATE_SWAP,

    /**
     * Equity Swap (ESTRD) - Equity returns ↔ Funding rate
     */
    EQUITY_SWAP,

    /**
     * FX Forward - Currency forwards
     */
    FX_FORWARD,

    /**
     * Equity Option - Call/Put options on stocks/indices
     */
    EQUITY_OPTION,

    /**
     * Credit Default Swap - Credit protection instruments
     */
    CREDIT_DEFAULT_SWAP
}