package io.annapurna.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Credit Default Swap (CDS) model.
 *
 * A contract where the protection buyer pays a periodic premium to the protection seller
 * in exchange for compensation if a credit event (default, bankruptcy, etc.) occurs
 * on a reference entity.
 *
 * Common use: Hedging credit risk, speculation on credit quality
 */
public class CreditDefaultSwap extends Trade {

    /**
     * Reference entity (the entity whose credit risk is being traded)
     * Examples: "Tesla Inc", "Republic of Argentina", "Bank of America"
     */
    private String referenceEntity;

    /**
     * Ticker/identifier for the reference entity
     * Examples: "TSLA", "ARG", "BAC"
     */
    private String referenceTicker;

    /**
     * Sector of the reference entity
     * Examples: "AUTOMOTIVE", "SOVEREIGN", "FINANCIALS", "TECHNOLOGY"
     */
    private String sector;

    /**
     * Credit rating of reference entity
     * Examples: "AAA", "AA", "A", "BBB", "BB", "B", "CCC"
     */
    private String creditRating;

    /**
     * CDS spread in basis points (annual cost of protection)
     * Example: 150 means 1.50% per year
     */
    private Integer spreadBps;

    /**
     * Upfront payment (for distressed credits)
     * Usually 0 for investment grade
     */
    private BigDecimal upfrontPayment;

    /**
     * Recovery rate assumption (% recovered in case of default)
     * Standard assumption: 40% for senior unsecured debt
     */
    private BigDecimal recoveryRate;

    /**
     * Payment frequency for premium
     * Typically "QUARTERLY"
     */
    private String paymentFrequency;

    /**
     * Position from our perspective
     * PROTECTION_BUYER: We pay spread, receive payout if default
     * PROTECTION_SELLER: We receive spread, pay if default
     */
    private String position;

    /**
     * Restructuring clause
     * Examples: "CR" (Full Restructuring), "MR" (Modified Restructuring),
     *           "MM" (Modified-Modified Restructuring), "XR" (No Restructuring)
     */
    private String restructuringClause;

    /**
     * Seniority of reference obligation
     * Examples: "SENIOR_UNSECURED", "SUBORDINATED", "SENIOR_SECURED"
     */
    private String seniority;

    public CreditDefaultSwap() {
        super();
    }

    // Getters and Setters
    public String getReferenceEntity() {
        return referenceEntity;
    }

    public void setReferenceEntity(String referenceEntity) {
        this.referenceEntity = referenceEntity;
    }

    public String getReferenceTicker() {
        return referenceTicker;
    }

    public void setReferenceTicker(String referenceTicker) {
        this.referenceTicker = referenceTicker;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getCreditRating() {
        return creditRating;
    }

    public void setCreditRating(String creditRating) {
        this.creditRating = creditRating;
    }

    public Integer getSpreadBps() {
        return spreadBps;
    }

    public void setSpreadBps(Integer spreadBps) {
        this.spreadBps = spreadBps;
    }

    public BigDecimal getUpfrontPayment() {
        return upfrontPayment;
    }

    public void setUpfrontPayment(BigDecimal upfrontPayment) {
        this.upfrontPayment = upfrontPayment;
    }

    public BigDecimal getRecoveryRate() {
        return recoveryRate;
    }

    public void setRecoveryRate(BigDecimal recoveryRate) {
        this.recoveryRate = recoveryRate;
    }

    public String getPaymentFrequency() {
        return paymentFrequency;
    }

    public void setPaymentFrequency(String paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getRestructuringClause() {
        return restructuringClause;
    }

    public void setRestructuringClause(String restructuringClause) {
        this.restructuringClause = restructuringClause;
    }

    public String getSeniority() {
        return seniority;
    }

    public void setSeniority(String seniority) {
        this.seniority = seniority;
    }

    @Override
    public String toString() {
        return "CreditDefaultSwap{" +
                "tradeId='" + getTradeId() + '\'' +
                ", referenceEntity='" + referenceEntity + '\'' +
                ", creditRating='" + creditRating + '\'' +
                ", spreadBps=" + spreadBps +
                ", notional=" + getNotional() +
                ", position='" + position + '\'' +
                ", maturityDate=" + getMaturityDate() +
                '}';
    }

    /**
     * Builder for fluent construction
     */
    public static class Builder {
        private final CreditDefaultSwap cds;

        public Builder() {
            this.cds = new CreditDefaultSwap();
            this.cds.setTradeType(TradeType.CREDIT_DEFAULT_SWAP);
        }

        public Builder tradeId(String tradeId) {
            cds.setTradeId(tradeId);
            return this;
        }

        public Builder tradeDate(LocalDate tradeDate) {
            cds.setTradeDate(tradeDate);
            return this;
        }

        public Builder settlementDate(LocalDate settlementDate) {
            cds.setSettlementDate(settlementDate);
            return this;
        }

        public Builder maturityDate(LocalDate maturityDate) {
            cds.setMaturityDate(maturityDate);
            return this;
        }

        public Builder notional(BigDecimal notional) {
            cds.setNotional(notional);
            return this;
        }

        public Builder currency(Currency currency) {
            cds.setCurrency(currency);
            return this;
        }

        public Builder counterparty(String counterparty) {
            cds.setCounterparty(counterparty);
            return this;
        }

        public Builder book(String book) {
            cds.setBook(book);
            return this;
        }

        public Builder trader(String trader) {
            cds.setTrader(trader);
            return this;
        }

        public Builder referenceEntity(String referenceEntity) {
            cds.setReferenceEntity(referenceEntity);
            return this;
        }

        public Builder referenceTicker(String referenceTicker) {
            cds.setReferenceTicker(referenceTicker);
            return this;
        }

        public Builder sector(String sector) {
            cds.setSector(sector);
            return this;
        }

        public Builder creditRating(String creditRating) {
            cds.setCreditRating(creditRating);
            return this;
        }

        public Builder spreadBps(Integer spreadBps) {
            cds.setSpreadBps(spreadBps);
            return this;
        }

        public Builder upfrontPayment(BigDecimal upfrontPayment) {
            cds.setUpfrontPayment(upfrontPayment);
            return this;
        }

        public Builder recoveryRate(BigDecimal recoveryRate) {
            cds.setRecoveryRate(recoveryRate);
            return this;
        }

        public Builder paymentFrequency(String paymentFrequency) {
            cds.setPaymentFrequency(paymentFrequency);
            return this;
        }

        public Builder position(String position) {
            cds.setPosition(position);
            return this;
        }

        public Builder restructuringClause(String restructuringClause) {
            cds.setRestructuringClause(restructuringClause);
            return this;
        }

        public Builder seniority(String seniority) {
            cds.setSeniority(seniority);
            return this;
        }

        public CreditDefaultSwap build() {
            return cds;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}