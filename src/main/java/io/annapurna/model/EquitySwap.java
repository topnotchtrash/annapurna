package io.annapurna.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Equity Swap (ESTRD - Equity Swap Trade) model.
 *
 * An equity swap is a derivative contract where one party exchanges (swaps) the returns
 * of an equity asset for another stream of cash flows (typically a funding rate).
 *
 */
public class EquitySwap extends Trade {

    /**
     * Reference asset (underlying equity)
     * Examples: "AAPL", "SPX", "MSFT", "GOOGL"
     */
    private String referenceAsset;

    /**
     * Return type for the equity leg
     * TOTAL_RETURN: price appreciation + dividends
     * PRICE_RETURN: price appreciation only
     */
    private String returnType;

    /**
     * Funding leg specification
     * Examples: "SOFR + 75bps", "LIBOR + 150bps"
     */
    private String fundingLeg;

    /**
     * Funding spread in basis points (bps)
     * Typical range: 50-200 bps based on notional and counterparty tier
     */
    private Integer fundingSpreadBps;

    /**
     * Settlement frequency
     * Examples: "MONTHLY", "QUARTERLY", "AT_MATURITY"
     */
    private String settlementFrequency;

    /**
     * Initial stock price (for valuation)
     */
    private BigDecimal initialPrice;

    /**
     * Number of shares/units (notional / initial price)
     */
    private BigDecimal quantity;

    /**
     * Direction: LONG or SHORT from the perspective of the equity leg
     */
    private String direction;

    // Constructors
    public EquitySwap() {
        super();
        setTradeType(TradeType.EQUITY_SWAP);
    }

    // Getters and Setters
    public String getReferenceAsset() {
        return referenceAsset;
    }

    public void setReferenceAsset(String referenceAsset) {
        this.referenceAsset = referenceAsset;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getFundingLeg() {
        return fundingLeg;
    }

    public void setFundingLeg(String fundingLeg) {
        this.fundingLeg = fundingLeg;
    }

    public Integer getFundingSpreadBps() {
        return fundingSpreadBps;
    }

    public void setFundingSpreadBps(Integer fundingSpreadBps) {
        this.fundingSpreadBps = fundingSpreadBps;
    }

    public String getSettlementFrequency() {
        return settlementFrequency;
    }

    public void setSettlementFrequency(String settlementFrequency) {
        this.settlementFrequency = settlementFrequency;
    }

    public BigDecimal getInitialPrice() {
        return initialPrice;
    }

    public void setInitialPrice(BigDecimal initialPrice) {
        this.initialPrice = initialPrice;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "EquitySwap{" +
                "tradeId='" + getTradeId() + '\'' +
                ", referenceAsset='" + referenceAsset + '\'' +
                ", notional=" + getNotional() +
                ", currency=" + getCurrency() +
                ", returnType='" + returnType + '\'' +
                ", fundingLeg='" + fundingLeg + '\'' +
                ", maturityDate=" + getMaturityDate() +
                ", counterparty='" + getCounterparty() + '\'' +
                '}';
    }

    /**
     * Builder for fluent construction of EquitySwap objects
     */
    public static class Builder {
        private final EquitySwap swap;

        public Builder() {
            this.swap = new EquitySwap();
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

        public Builder referenceAsset(String referenceAsset) {
            swap.setReferenceAsset(referenceAsset);
            return this;
        }

        public Builder returnType(String returnType) {
            swap.setReturnType(returnType);
            return this;
        }

        public Builder fundingLeg(String fundingLeg) {
            swap.setFundingLeg(fundingLeg);
            return this;
        }

        public Builder fundingSpreadBps(Integer fundingSpreadBps) {
            swap.setFundingSpreadBps(fundingSpreadBps);
            return this;
        }

        public Builder settlementFrequency(String settlementFrequency) {
            swap.setSettlementFrequency(settlementFrequency);
            return this;
        }

        public Builder initialPrice(BigDecimal initialPrice) {
            swap.setInitialPrice(initialPrice);
            return this;
        }

        public Builder quantity(BigDecimal quantity) {
            swap.setQuantity(quantity);
            return this;
        }

        public Builder direction(String direction) {
            swap.setDirection(direction);
            return this;
        }

        public EquitySwap build() {
            return swap;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}