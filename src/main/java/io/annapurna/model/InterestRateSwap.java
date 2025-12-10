package io.annapurna.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Interest Rate Swap model.
 *
 * A swap where one party pays a fixed interest rate and receives a floating rate,
 * while the counterparty does the opposite.
 *
 * Common use: Hedging interest rate risk, speculation on rate movements
 */
public class InterestRateSwap extends Trade {

    /**
     * Fixed rate leg rate (annual percentage)
     * Example: 3.50 (means 3.50% per annum)
     */
    private BigDecimal fixedRate;

    /**
     * Floating rate index
     * Examples: "SOFR", "LIBOR", "EURIBOR"
     */
    private String floatingRateIndex;

    /**
     * Spread added to floating rate (in basis points)
     * Example: 25 means SOFR + 25bps
     */
    private Integer floatingSpreadBps;

    /**
     * Payment frequency for fixed leg
     * Examples: "QUARTERLY", "SEMI_ANNUAL", "ANNUAL"
     */
    private String fixedLegFrequency;

    /**
     * Payment frequency for floating leg
     * Examples: "QUARTERLY", "SEMI_ANNUAL"
     */
    private String floatingLegFrequency;

    /**
     * Day count convention
     * Examples: "ACT/360", "ACT/365", "30/360"
     */
    private String dayCountConvention;

    /**
     * Direction from our perspective
     * PAY_FIXED: We pay fixed, receive floating
     * RECEIVE_FIXED: We receive fixed, pay floating
     */
    private String direction;

    /**
     * Effective date when swap starts accruing
     */
    private LocalDate effectiveDate;

    public InterestRateSwap() {
        super();
    }

    // Getters and Setters
    public BigDecimal getFixedRate() {
        return fixedRate;
    }

    public void setFixedRate(BigDecimal fixedRate) {
        this.fixedRate = fixedRate;
    }

    public String getFloatingRateIndex() {
        return floatingRateIndex;
    }

    public void setFloatingRateIndex(String floatingRateIndex) {
        this.floatingRateIndex = floatingRateIndex;
    }

    public Integer getFloatingSpreadBps() {
        return floatingSpreadBps;
    }

    public void setFloatingSpreadBps(Integer floatingSpreadBps) {
        this.floatingSpreadBps = floatingSpreadBps;
    }

    public String getFixedLegFrequency() {
        return fixedLegFrequency;
    }

    public void setFixedLegFrequency(String fixedLegFrequency) {
        this.fixedLegFrequency = fixedLegFrequency;
    }

    public String getFloatingLegFrequency() {
        return floatingLegFrequency;
    }

    public void setFloatingLegFrequency(String floatingLegFrequency) {
        this.floatingLegFrequency = floatingLegFrequency;
    }

    public String getDayCountConvention() {
        return dayCountConvention;
    }

    public void setDayCountConvention(String dayCountConvention) {
        this.dayCountConvention = dayCountConvention;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    @Override
    public String toString() {
        return "InterestRateSwap{" +
                "tradeId='" + getTradeId() + '\'' +
                ", notional=" + getNotional() +
                ", currency=" + getCurrency() +
                ", fixedRate=" + fixedRate +
                ", floatingIndex='" + floatingRateIndex + '\'' +
                ", direction='" + direction + '\'' +
                ", maturityDate=" + getMaturityDate() +
                '}';
    }

    /**
     * Builder for fluent construction
     */
    public static class Builder {
        private final InterestRateSwap swap;

        public Builder() {
            this.swap = new InterestRateSwap();
            this.swap.setTradeType(TradeType.INTEREST_RATE_SWAP);
        }

        public Builder tradeId(String tradeId) {
            swap.setTradeId(tradeId);
            return this;
        }

        public Builder tradeDate(LocalDate tradeDate) {
            swap.setTradeDate(tradeDate);
            return this;
        }

        public Builder settlementDate(LocalDate settlementDate) {
            swap.setSettlementDate(settlementDate);
            return this;
        }

        public Builder maturityDate(LocalDate maturityDate) {
            swap.setMaturityDate(maturityDate);
            return this;
        }

        public Builder notional(BigDecimal notional) {
            swap.setNotional(notional);
            return this;
        }

        public Builder currency(Currency currency) {
            swap.setCurrency(currency);
            return this;
        }

        public Builder counterparty(String counterparty) {
            swap.setCounterparty(counterparty);
            return this;
        }

        public Builder book(String book) {
            swap.setBook(book);
            return this;
        }

        public Builder trader(String trader) {
            swap.setTrader(trader);
            return this;
        }

        public Builder fixedRate(BigDecimal fixedRate) {
            swap.setFixedRate(fixedRate);
            return this;
        }

        public Builder floatingRateIndex(String floatingRateIndex) {
            swap.setFloatingRateIndex(floatingRateIndex);
            return this;
        }

        public Builder floatingSpreadBps(Integer floatingSpreadBps) {
            swap.setFloatingSpreadBps(floatingSpreadBps);
            return this;
        }

        public Builder fixedLegFrequency(String fixedLegFrequency) {
            swap.setFixedLegFrequency(fixedLegFrequency);
            return this;
        }

        public Builder floatingLegFrequency(String floatingLegFrequency) {
            swap.setFloatingLegFrequency(floatingLegFrequency);
            return this;
        }

        public Builder dayCountConvention(String dayCountConvention) {
            swap.setDayCountConvention(dayCountConvention);
            return this;
        }

        public Builder direction(String direction) {
            swap.setDirection(direction);
            return this;
        }

        public Builder effectiveDate(LocalDate effectiveDate) {
            swap.setEffectiveDate(effectiveDate);
            return this;
        }

        public InterestRateSwap build() {
            return swap;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}