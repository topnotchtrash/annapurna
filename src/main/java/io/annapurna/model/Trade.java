package io.annapurna.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Abstract base class for all trade types.
 * Contains common fields shared across all financial instruments.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "tradeType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EquitySwap.class, name = "EQUITY_SWAP")
        // Other types will be added as we implement them
})
public abstract class Trade {

    private String tradeId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tradeDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate settlementDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate maturityDate;

    @JsonIgnore
    private TradeType tradeType;

    private BigDecimal notional;

    private Currency currency;

    private String counterparty;

    private String book;

    private String trader;

    // Constructors
    protected Trade() {
    }

    protected Trade(String tradeId, LocalDate tradeDate, LocalDate settlementDate,
                    LocalDate maturityDate, TradeType tradeType, BigDecimal notional,
                    Currency currency, String counterparty, String book, String trader) {
        this.tradeId = tradeId;
        this.tradeDate = tradeDate;
        this.settlementDate = settlementDate;
        this.maturityDate = maturityDate;
        this.tradeType = tradeType;
        this.notional = notional;
        this.currency = currency;
        this.counterparty = counterparty;
        this.book = book;
        this.trader = trader;
    }

    // Getters and Setters
    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(LocalDate settlementDate) {
        this.settlementDate = settlementDate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
    }

    public TradeType getTradeType() {
        return tradeType;
    }

    public void setTradeType(TradeType tradeType) {
        this.tradeType = tradeType;
    }

    public BigDecimal getNotional() {
        return notional;
    }

    public void setNotional(BigDecimal notional) {
        this.notional = notional;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(String counterparty) {
        this.counterparty = counterparty;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public String getTrader() {
        return trader;
    }

    public void setTrader(String trader) {
        this.trader = trader;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        return Objects.equals(tradeId, trade.tradeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeId);
    }

    @Override
    public String toString() {
        return "Trade{" +
                "tradeId='" + tradeId + '\'' +
                ", tradeDate=" + tradeDate +
                ", tradeType=" + tradeType +
                ", notional=" + notional +
                ", currency=" + currency +
                '}';
    }
}