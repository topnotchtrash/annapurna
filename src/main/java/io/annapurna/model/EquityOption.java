package io.annapurna.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Equity Option model.
 *
 * A contract giving the holder the right (but not obligation) to buy (call)
 * or sell (put) an underlying equity at a specified price on or before expiry.
 *
 * Common use: Hedging equity positions, speculation, income generation
 */
public class EquityOption extends Trade {

    /**
     * Underlying asset
     * Examples: "AAPL", "SPX", "MSFT", "TSLA"
     */
    private String underlyingAsset;

    /**
     * Option type
     * CALL: Right to buy
     * PUT: Right to sell
     */
    private String optionType;

    /**
     * Strike price (exercise price)
     */
    private BigDecimal strikePrice;

    /**
     * Current spot price of underlying
     */
    private BigDecimal spotPrice;

    /**
     * Option premium (price paid for the option)
     * Usually expressed per share
     */
    private BigDecimal premium;

    /**
     * Number of contracts
     * Each contract typically represents 100 shares
     */
    private Integer contracts;

    /**
     * Total number of shares covered
     * Usually contracts * 100
     */
    private BigDecimal quantity;

    /**
     * Expiry date (last day to exercise)
     */
    private LocalDate expiryDate;

    /**
     * Style of option
     * AMERICAN: Can exercise anytime before expiry
     * EUROPEAN: Can only exercise at expiry
     */
    private String exerciseStyle;

    /**
     * Moneyness at trade time
     * ATM: At The Money (spot ≈ strike)
     * ITM: In The Money (call: spot > strike, put: spot < strike)
     * OTM: Out of The Money (call: spot < strike, put: spot > strike)
     */
    private String moneyness;

    /**
     * Position direction
     * LONG: Bought the option (paid premium)
     * SHORT: Sold the option (received premium)
     */
    private String position;

    /**
     * Implied volatility at trade time (annualized %)
     */
    private BigDecimal impliedVolatility;

    public EquityOption() {
        super();
    }

    // Getters and Setters
    public String getUnderlyingAsset() {
        return underlyingAsset;
    }

    public void setUnderlyingAsset(String underlyingAsset) {
        this.underlyingAsset = underlyingAsset;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    public BigDecimal getStrikePrice() {
        return strikePrice;
    }

    public void setStrikePrice(BigDecimal strikePrice) {
        this.strikePrice = strikePrice;
    }

    public BigDecimal getSpotPrice() {
        return spotPrice;
    }

    public void setSpotPrice(BigDecimal spotPrice) {
        this.spotPrice = spotPrice;
    }

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
    }

    public Integer getContracts() {
        return contracts;
    }

    public void setContracts(Integer contracts) {
        this.contracts = contracts;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getExerciseStyle() {
        return exerciseStyle;
    }

    public void setExerciseStyle(String exerciseStyle) {
        this.exerciseStyle = exerciseStyle;
    }

    public String getMoneyness() {
        return moneyness;
    }

    public void setMoneyness(String moneyness) {
        this.moneyness = moneyness;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public BigDecimal getImpliedVolatility() {
        return impliedVolatility;
    }

    public void setImpliedVolatility(BigDecimal impliedVolatility) {
        this.impliedVolatility = impliedVolatility;
    }

    @Override
    public String toString() {
        return "EquityOption{" +
                "tradeId='" + getTradeId() + '\'' +
                ", underlyingAsset='" + underlyingAsset + '\'' +
                ", optionType='" + optionType + '\'' +
                ", strikePrice=" + strikePrice +
                ", spotPrice=" + spotPrice +
                ", premium=" + premium +
                ", contracts=" + contracts +
                ", expiryDate=" + expiryDate +
                ", moneyness='" + moneyness + '\'' +
                '}';
    }

    /**
     * Builder for fluent construction
     */
    public static class Builder {
        private final EquityOption option;

        public Builder() {
            this.option = new EquityOption();
            this.option.setTradeType(TradeType.EQUITY_OPTION);
        }

        public Builder tradeId(String tradeId) {
            option.setTradeId(tradeId);
            return this;
        }

        public Builder tradeDate(LocalDate tradeDate) {
            option.setTradeDate(tradeDate);
            return this;
        }

        public Builder settlementDate(LocalDate settlementDate) {
            option.setSettlementDate(settlementDate);
            return this;
        }

        public Builder maturityDate(LocalDate maturityDate) {
            option.setMaturityDate(maturityDate);
            return this;
        }

        public Builder notional(BigDecimal notional) {
            option.setNotional(notional);
            return this;
        }

        public Builder currency(Currency currency) {
            option.setCurrency(currency);
            return this;
        }

        public Builder counterparty(String counterparty) {
            option.setCounterparty(counterparty);
            return this;
        }

        public Builder book(String book) {
            option.setBook(book);
            return this;
        }

        public Builder trader(String trader) {
            option.setTrader(trader);
            return this;
        }

        public Builder underlyingAsset(String underlyingAsset) {
            option.setUnderlyingAsset(underlyingAsset);
            return this;
        }

        public Builder optionType(String optionType) {
            option.setOptionType(optionType);
            return this;
        }

        public Builder strikePrice(BigDecimal strikePrice) {
            option.setStrikePrice(strikePrice);
            return this;
        }

        public Builder spotPrice(BigDecimal spotPrice) {
            option.setSpotPrice(spotPrice);
            return this;
        }

        public Builder premium(BigDecimal premium) {
            option.setPremium(premium);
            return this;
        }

        public Builder contracts(Integer contracts) {
            option.setContracts(contracts);
            return this;
        }

        public Builder quantity(BigDecimal quantity) {
            option.setQuantity(quantity);
            return this;
        }

        public Builder expiryDate(LocalDate expiryDate) {
            option.setExpiryDate(expiryDate);
            return this;
        }

        public Builder exerciseStyle(String exerciseStyle) {
            option.setExerciseStyle(exerciseStyle);
            return this;
        }

        public Builder moneyness(String moneyness) {
            option.setMoneyness(moneyness);
            return this;
        }

        public Builder position(String position) {
            option.setPosition(position);
            return this;
        }

        public Builder impliedVolatility(BigDecimal impliedVolatility) {
            option.setImpliedVolatility(impliedVolatility);
            return this;
        }

        public EquityOption build() {
            return option;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}