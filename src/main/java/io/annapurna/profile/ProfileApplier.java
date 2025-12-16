package io.annapurna.profile;

import io.annapurna.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Applies data quality corruption to trades based on profile.
 *
 * Corruption levels:
 * - CLEAN: No corruption (returns original trade)
 * - EDGE_CASE: 1 corruption (missing optional field or extreme value)
 * - STRESS: 2-4 corruptions (broken data, invalid state)
 *
 * Thread-safe: Uses ThreadLocalRandom for parallel generation.
 */
public class ProfileApplier {

    /**
     * Apply corruption to a trade based on profile.
     * Includes both general and trade-type-specific corruptions.
     *
     * @param trade Original trade
     * @param profile Data quality profile
     * @return Corrupted trade (or original if CLEAN)
     */
    public static Trade apply(Trade trade, DataProfile profile) {
        if (profile == DataProfile.CLEAN) {
            return trade; // No corruption
        }

        // Apply general corruption
        trade = applyGeneralCorruption(trade, profile);

        // Apply type-specific corruption
        trade = applyTypeSpecificCorruption(trade, profile);

        return trade;
    }

    /**
     * Apply general corruptions that work for all trade types.
     */
    private static Trade applyGeneralCorruption(Trade trade, DataProfile profile) {
        if (profile == DataProfile.EDGE_CASE) {
            return applyEdgeCaseCorruption(trade);
        }

        if (profile == DataProfile.STRESS) {
            return applyStressCorruption(trade);
        }

        return trade;
    }

    /**
     * Apply edge case corruption - ONE issue, still technically valid.
     */
    private static Trade applyEdgeCaseCorruption(Trade trade) {
        int corruption = ThreadLocalRandom.current().nextInt(5);

        switch (corruption) {
            case 0:
                // Missing counterparty (optional field)
                trade.setCounterparty(null);
                break;

            case 1:
                // Missing trader (optional field)
                trade.setTrader(null);
                break;

            case 2:
                // Extreme notional - very small
                trade.setNotional(BigDecimal.ONE);
                break;

            case 3:
                // Extreme notional - very large
                trade.setNotional(BigDecimal.valueOf(10_000_000_000L)); // $10B
                break;

            case 4:
                // Missing book (optional field)
                trade.setBook(null);
                break;
        }

        return trade;
    }

    /**
     * Apply stress corruption - MULTIPLE issues, completely broken.
     */
    private static Trade applyStressCorruption(Trade trade) {
        int numCorruptions = 2 + ThreadLocalRandom.current().nextInt(3); // 2-4 corruptions

        for (int i = 0; i < numCorruptions; i++) {
            int corruption = ThreadLocalRandom.current().nextInt(10);

            switch (corruption) {
                case 0:
                    // Missing currency (CRITICAL)
                    trade.setCurrency(null);
                    break;

                case 1:
                    // Missing trade date (CRITICAL)
                    trade.setTradeDate(null);
                    break;

                case 2:
                    // Settlement before trade date (INVALID)
                    if (trade.getTradeDate() != null) {
                        trade.setSettlementDate(trade.getTradeDate().minusDays(5));
                    }
                    break;

                case 3:
                    // Maturity before settlement (INVALID)
                    if (trade.getSettlementDate() != null) {
                        trade.setMaturityDate(trade.getSettlementDate().minusDays(10));
                    }
                    break;

                case 4:
                    // Negative notional (INVALID)
                    trade.setNotional(BigDecimal.valueOf(-1_000_000));
                    break;

                case 5:
                    // Zero notional (INVALID)
                    trade.setNotional(BigDecimal.ZERO);
                    break;

                case 6:
                    // Future trade date (INVALID)
                    trade.setTradeDate(LocalDate.now().plusYears(1));
                    break;

                case 7:
                    // Missing counterparty (CRITICAL for some systems)
                    trade.setCounterparty(null);
                    break;

                case 8:
                    // Missing trade ID (CRITICAL)
                    trade.setTradeId(null);
                    break;

                case 9:
                    // Null notional (CRITICAL)
                    trade.setNotional(null);
                    break;
            }
        }

        return trade;
    }

    /**
     * Apply trade-type specific corruptions.
     */
    private static Trade applyTypeSpecificCorruption(Trade trade, DataProfile profile) {
        if (trade instanceof EquitySwap) {
            return corruptEquitySwap((EquitySwap) trade, profile);
        } else if (trade instanceof InterestRateSwap) {
            return corruptInterestRateSwap((InterestRateSwap) trade, profile);
        } else if (trade instanceof FXForward) {
            return corruptFXForward((FXForward) trade, profile);
        } else if (trade instanceof EquityOption) {
            return corruptEquityOption((EquityOption) trade, profile);
        } else if (trade instanceof CreditDefaultSwap) {
            return corruptCDS((CreditDefaultSwap) trade, profile);
        }

        return trade;
    }

    /**
     * Corrupt Equity Swap specific fields.
     */
    private static EquitySwap corruptEquitySwap(EquitySwap swap, DataProfile profile) {
        if (profile == DataProfile.EDGE_CASE) {
            int corruption = ThreadLocalRandom.current().nextInt(3);
            switch (corruption) {
                case 0:
                    // Missing reference asset
                    swap.setReferenceAsset(null);
                    break;
                case 1:
                    // Extreme funding spread
                    swap.setFundingSpreadBps(5000); // 50% spread
                    break;
                case 2:
                    // Missing return type
                    swap.setReturnType(null);
                    break;
            }
        } else if (profile == DataProfile.STRESS) {
            int corruption = ThreadLocalRandom.current().nextInt(3);
            switch (corruption) {
                case 0:
                    // Broken math: quantity * price != notional
                    swap.setQuantity(BigDecimal.valueOf(1000));
                    swap.setInitialPrice(BigDecimal.valueOf(100));
                    swap.setNotional(BigDecimal.valueOf(50_000_000)); // Doesn't match!
                    break;
                case 1:
                    // Negative funding spread
                    swap.setFundingSpreadBps(-500);
                    break;
                case 2:
                    // Zero or negative price
                    swap.setInitialPrice(BigDecimal.valueOf(-50));
                    break;
            }
        }
        return swap;
    }

    /**
     * Corrupt Interest Rate Swap specific fields.
     */
    private static InterestRateSwap corruptInterestRateSwap(InterestRateSwap swap, DataProfile profile) {
        if (profile == DataProfile.EDGE_CASE) {
            int corruption = ThreadLocalRandom.current().nextInt(3);
            switch (corruption) {
                case 0:
                    // Missing floating rate index
                    swap.setFloatingRateIndex(null);
                    break;
                case 1:
                    // Extreme fixed rate
                    swap.setFixedRate(BigDecimal.valueOf(25.0)); // 25% fixed rate
                    break;
                case 2:
                    // Missing day count convention
                    swap.setDayCountConvention(null);
                    break;
            }
        } else if (profile == DataProfile.STRESS) {
            int corruption = ThreadLocalRandom.current().nextInt(3);
            switch (corruption) {
                case 0:
                    // Negative fixed rate
                    swap.setFixedRate(BigDecimal.valueOf(-2.5));
                    break;
                case 1:
                    // Missing both fixed rate and floating rate
                    swap.setFixedRate(null);
                    swap.setFloatingRateIndex(null);
                    break;
                case 2:
                    // Extreme negative spread
                    swap.setFloatingSpreadBps(-10000);
                    break;
            }
        }
        return swap;
    }

    /**
     * Corrupt FX Forward specific fields.
     */
    private static FXForward corruptFXForward(FXForward forward, DataProfile profile) {
        if (profile == DataProfile.EDGE_CASE) {
            int corruption = ThreadLocalRandom.current().nextInt(3);
            switch (corruption) {
                case 0:
                    // Missing currency pair
                    forward.setCurrencyPair(null);
                    break;
                case 1:
                    // Extreme forward points
                    forward.setForwardPoints(BigDecimal.valueOf(10000));
                    break;
                case 2:
                    // Same base and quote currency
                    forward.setBaseCurrency(forward.getQuoteCurrency());
                    break;
            }
        } else if (profile == DataProfile.STRESS) {
            int corruption = ThreadLocalRandom.current().nextInt(3);
            switch (corruption) {
                case 0:
                    // Forward rate = spot rate (no forward points, invalid)
                    forward.setForwardRate(forward.getSpotRate());
                    forward.setForwardPoints(BigDecimal.ZERO);
                    break;
                case 1:
                    // Negative spot rate
                    forward.setSpotRate(BigDecimal.valueOf(-1.5));
                    break;
                case 2:
                    // Missing both currencies
                    forward.setBaseCurrency(null);
                    forward.setQuoteCurrency(null);
                    break;
            }
        }
        return forward;
    }

    /**
     * Corrupt Equity Option specific fields.
     */
    private static EquityOption corruptEquityOption(EquityOption option, DataProfile profile) {
        if (profile == DataProfile.EDGE_CASE) {
            int corruption = ThreadLocalRandom.current().nextInt(3);
            switch (corruption) {
                case 0:
                    // Missing strike price
                    option.setStrikePrice(null);
                    break;
                case 1:
                    // Zero premium (free option)
                    option.setPremium(BigDecimal.ZERO);
                    break;
                case 2:
                    // Missing underlying asset
                    option.setUnderlyingAsset(null);
                    break;
            }
        } else if (profile == DataProfile.STRESS) {
            int corruption = ThreadLocalRandom.current().nextInt(3);
            switch (corruption) {
                case 0:
                    // Expiry in the past (expired option being traded)
                    option.setExpiryDate(LocalDate.now().minusYears(1));
                    break;
                case 1:
                    // Negative strike price
                    option.setStrikePrice(BigDecimal.valueOf(-100));
                    break;
                case 2:
                    // Strike = spot (but marked as OTM - inconsistent)
                    option.setStrikePrice(option.getSpotPrice());
                    option.setMoneyness("OTM");
                    break;
            }
        }
        return option;
    }

    /**
     * Corrupt Credit Default Swap specific fields.
     */
    private static CreditDefaultSwap corruptCDS(CreditDefaultSwap cds, DataProfile profile) {
        if (profile == DataProfile.EDGE_CASE) {
            int corruption = ThreadLocalRandom.current().nextInt(3);
            switch (corruption) {
                case 0:
                    // Missing reference entity
                    cds.setReferenceEntity(null);
                    break;
                case 1:
                    // Extreme spread (distressed credit)
                    cds.setSpreadBps(5000); // 50% spread
                    break;
                case 2:
                    // Missing sector
                    cds.setSector(null);
                    break;
            }
        } else if (profile == DataProfile.STRESS) {
            int corruption = ThreadLocalRandom.current().nextInt(3);
            switch (corruption) {
                case 0:
                    // Negative spread (impossible)
                    cds.setSpreadBps(-100);
                    break;
                case 1:
                    // Recovery rate > 100% (impossible)
                    cds.setRecoveryRate(BigDecimal.valueOf(150));
                    break;
                case 2:
                    // Missing both reference entity and ticker
                    cds.setReferenceEntity(null);
                    cds.setReferenceTicker(null);
                    break;
            }
        }
        return cds;
    }
}