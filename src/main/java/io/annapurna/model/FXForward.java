package io.annapurna.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * FX Forward model.
 *
 * A contract to exchange one currency for another at a future date
 * at a predetermined exchange rate.
 *
 * Common use: Hedging foreign exchange exposure, speculation on currency movements
 */
public class FXForward extends Trade {

    /**
     * Currency pair
     * Format: "BASE/QUOTE" e.g., "EUR/USD", "USD/JPY"
     */
    private String currencyPair;

    /**
     * Base currency (the one being bought/sold)
     */
    private Currency baseCurrency;

    /**
     * Quote currency (the one used for pricing)
     */
    private Currency quoteCurrency;

    /**
     * Spot exchange rate at trade time
     * Example: 1.0850 for EUR/USD means 1 EUR = 1.0850 USD
     */
    private BigDecimal spotRate;

    /**
     * Forward exchange rate (contracted rate for future settlement)
     */
    private BigDecimal forwardRate;

    /**
     * Forward points (difference between forward and spot, in pips)
     * Positive = premium, Negative = discount
     */
    private BigDecimal forwardPoints;

    /**
     * Direction from our perspective
     * BUY: We buy base currency (sell quote currency)
     * SELL: We sell base currency (buy quote currency)
     */
    private String direction;

    /**
     * Settlement type
     * PHYSICAL: Actual currency delivery
     * NON_DELIVERABLE: Cash settlement (NDF)
     */
    private String settlementType;

    public FXForward() {
        super();
    }

    // Getters and Setters
    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(Currency baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public Currency getQuoteCurrency() {
        return quoteCurrency;
    }

    public void setQuoteCurrency(Currency quoteCurrency) {
        this.quoteCurrency = quoteCurrency;
    }

    public BigDecimal getSpotRate() {
        return spotRate;
    }

    public void setSpotRate(BigDecimal spotRate) {
        this.spotRate = spotRate;
    }

    public BigDecimal getForwardRate() {
        return forwardRate;
    }

    public void setForwardRate(BigDecimal forwardRate) {
        this.forwardRate = forwardRate;
    }

    public BigDecimal getForwardPoints() {
        return forwardPoints;
    }

    public void setForwardPoints(BigDecimal forwardPoints) {
        this.forwardPoints = forwardPoints;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(String settlementType) {
        this.settlementType = settlementType;
    }

    @Override
    public String toString() {
        return "FXForward{" +
                "tradeId='" + getTradeId() + '\'' +
                ", currencyPair='" + currencyPair + '\'' +
                ", notional=" + getNotional() +
                ", spotRate=" + spotRate +
                ", forwardRate=" + forwardRate +
                ", direction='" + direction + '\'' +
                ", maturityDate=" + getMaturityDate() +
                '}';
    }

    /**
     * Builder for fluent construction
     */
    public static class Builder {
        private final FXForward forward;

        public Builder() {
            this.forward = new FXForward();
            this.forward.setTradeType(TradeType.FX_FORWARD);
        }

        public Builder tradeId(String tradeId) {
            forward.setTradeId(tradeId);
            return this;
        }

        public Builder tradeDate(LocalDate tradeDate) {
            forward.setTradeDate(tradeDate);
            return this;
        }

        public Builder settlementDate(LocalDate settlementDate) {
            forward.setSettlementDate(settlementDate);
            return this;
        }

        public Builder maturityDate(LocalDate maturityDate) {
            forward.setMaturityDate(maturityDate);
            return this;
        }

        public Builder notional(BigDecimal notional) {
            forward.setNotional(notional);
            return this;
        }

        public Builder currency(Currency currency) {
            forward.setCurrency(currency);
            return this;
        }

        public Builder counterparty(String counterparty) {
            forward.setCounterparty(counterparty);
            return this;
        }

        public Builder book(String book) {
            forward.setBook(book);
            return this;
        }

        public Builder trader(String trader) {
            forward.setTrader(trader);
            return this;
        }

        public Builder currencyPair(String currencyPair) {
            forward.setCurrencyPair(currencyPair);
            return this;
        }

        public Builder baseCurrency(Currency baseCurrency) {
            forward.setBaseCurrency(baseCurrency);
            return this;
        }

        public Builder quoteCurrency(Currency quoteCurrency) {
            forward.setQuoteCurrency(quoteCurrency);
            return this;
        }

        public Builder spotRate(BigDecimal spotRate) {
            forward.setSpotRate(spotRate);
            return this;
        }

        public Builder forwardRate(BigDecimal forwardRate) {
            forward.setForwardRate(forwardRate);
            return this;
        }

        public Builder forwardPoints(BigDecimal forwardPoints) {
            forward.setForwardPoints(forwardPoints);
            return this;
        }

        public Builder direction(String direction) {
            forward.setDirection(direction);
            return this;
        }

        public Builder settlementType(String settlementType) {
            forward.setSettlementType(settlementType);
            return this;
        }

        public FXForward build() {
            return forward;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}