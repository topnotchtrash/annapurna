package io.annapurna.serialization;

import io.annapurna.model.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Serializer for exporting trades to CSV format.
 *
 * Creates separate CSV files for each trade type with appropriate columns.
 * Common fields appear first, followed by type-specific fields.
 */
public class CsvSerializer {

    /**
     * Write trades to CSV file.
     * All trades must be of the same type.
     *
     * @param trades List of trades to serialize
     * @param filepath Path to output file
     * @throws IOException if write fails
     * @throws IllegalArgumentException if trades contain mixed types
     */
    public void write(List<Trade> trades, String filepath) throws IOException {
        if (trades.isEmpty()) {
            throw new IllegalArgumentException("Trade list is empty");
        }

        // Determine trade type from first trade
        Trade firstTrade = trades.get(0);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            if (firstTrade instanceof EquitySwap) {
                writeEquitySwaps(trades, writer);
            } else if (firstTrade instanceof InterestRateSwap) {
                writeInterestRateSwaps(trades, writer);
            } else if (firstTrade instanceof FXForward) {
                writeFXForwards(trades, writer);
            } else if (firstTrade instanceof EquityOption) {
                writeEquityOptions(trades, writer);
            } else if (firstTrade instanceof CreditDefaultSwap) {
                writeCDS(trades, writer);
            } else {
                throw new IllegalArgumentException("Unknown trade type: " + firstTrade.getClass());
            }
        }
    }

    /**
     * Write trades grouped by type to separate CSV files.
     *
     * @param trades Mixed list of trades
     * @param baseFilepath Base path (will append trade type)
     * @throws IOException if write fails
     */
    public void writeByType(List<Trade> trades, String baseFilepath) throws IOException {
        // Group by type
        List<EquitySwap> equitySwaps = trades.stream()
                .filter(t -> t instanceof EquitySwap)
                .map(t -> (EquitySwap) t)
                .toList();

        List<InterestRateSwap> irs = trades.stream()
                .filter(t -> t instanceof InterestRateSwap)
                .map(t -> (InterestRateSwap) t)
                .toList();

        List<FXForward> fxForwards = trades.stream()
                .filter(t -> t instanceof FXForward)
                .map(t -> (FXForward) t)
                .toList();

        List<EquityOption> options = trades.stream()
                .filter(t -> t instanceof EquityOption)
                .map(t -> (EquityOption) t)
                .toList();

        List<CreditDefaultSwap> cds = trades.stream()
                .filter(t -> t instanceof CreditDefaultSwap)
                .map(t -> (CreditDefaultSwap) t)
                .toList();

        // Write each type to separate file
        if (!equitySwaps.isEmpty()) {
            write(equitySwaps.stream().map(t -> (Trade) t).toList(),
                    baseFilepath + "_equity_swaps.csv");
        }
        if (!irs.isEmpty()) {
            write(irs.stream().map(t -> (Trade) t).toList(),
                    baseFilepath + "_interest_rate_swaps.csv");
        }
        if (!fxForwards.isEmpty()) {
            write(fxForwards.stream().map(t -> (Trade) t).toList(),
                    baseFilepath + "_fx_forwards.csv");
        }
        if (!options.isEmpty()) {
            write(options.stream().map(t -> (Trade) t).toList(),
                    baseFilepath + "_equity_options.csv");
        }
        if (!cds.isEmpty()) {
            write(cds.stream().map(t -> (Trade) t).toList(),
                    baseFilepath + "_cds.csv");
        }
    }

    private void writeEquitySwaps(List<Trade> trades, BufferedWriter writer) throws IOException {
        // Header
        writer.write("tradeId,tradeDate,settlementDate,maturityDate,notional,currency,counterparty,book,trader," +
                "referenceAsset,returnType,fundingLeg,fundingSpreadBps,settlementFrequency," +
                "initialPrice,quantity,direction");
        writer.newLine();

        // Data
        for (Trade trade : trades) {
            EquitySwap swap = (EquitySwap) trade;
            writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    csvEscape(swap.getTradeId()),
                    swap.getTradeDate(),
                    swap.getSettlementDate(),
                    swap.getMaturityDate(),
                    swap.getNotional(),
                    swap.getCurrency(),
                    csvEscape(swap.getCounterparty()),
                    csvEscape(swap.getBook()),
                    csvEscape(swap.getTrader()),
                    csvEscape(swap.getReferenceAsset()),
                    swap.getReturnType(),
                    csvEscape(swap.getFundingLeg()),
                    swap.getFundingSpreadBps(),
                    swap.getSettlementFrequency(),
                    swap.getInitialPrice(),
                    swap.getQuantity(),
                    swap.getDirection()
            ));
            writer.newLine();
        }
    }

    private void writeInterestRateSwaps(List<Trade> trades, BufferedWriter writer) throws IOException {
        // Header
        writer.write("tradeId,tradeDate,settlementDate,maturityDate,notional,currency,counterparty,book,trader," +
                "fixedRate,floatingRateIndex,floatingSpreadBps,fixedLegFrequency,floatingLegFrequency," +
                "dayCountConvention,direction,effectiveDate");
        writer.newLine();

        // Data
        for (Trade trade : trades) {
            InterestRateSwap swap = (InterestRateSwap) trade;
            writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    csvEscape(swap.getTradeId()),
                    swap.getTradeDate(),
                    swap.getSettlementDate(),
                    swap.getMaturityDate(),
                    swap.getNotional(),
                    swap.getCurrency(),
                    csvEscape(swap.getCounterparty()),
                    csvEscape(swap.getBook()),
                    csvEscape(swap.getTrader()),
                    swap.getFixedRate(),
                    csvEscape(swap.getFloatingRateIndex()),
                    swap.getFloatingSpreadBps(),
                    swap.getFixedLegFrequency(),
                    swap.getFloatingLegFrequency(),
                    csvEscape(swap.getDayCountConvention()),
                    swap.getDirection(),
                    swap.getEffectiveDate()
            ));
            writer.newLine();
        }
    }

    private void writeFXForwards(List<Trade> trades, BufferedWriter writer) throws IOException {
        // Header
        writer.write("tradeId,tradeDate,settlementDate,maturityDate,notional,currency,counterparty,book,trader," +
                "currencyPair,baseCurrency,quoteCurrency,spotRate,forwardRate,forwardPoints," +
                "direction,settlementType");
        writer.newLine();

        // Data
        for (Trade trade : trades) {
            FXForward forward = (FXForward) trade;
            writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    csvEscape(forward.getTradeId()),
                    forward.getTradeDate(),
                    forward.getSettlementDate(),
                    forward.getMaturityDate(),
                    forward.getNotional(),
                    forward.getCurrency(),
                    csvEscape(forward.getCounterparty()),
                    csvEscape(forward.getBook()),
                    csvEscape(forward.getTrader()),
                    csvEscape(forward.getCurrencyPair()),
                    forward.getBaseCurrency(),
                    forward.getQuoteCurrency(),
                    forward.getSpotRate(),
                    forward.getForwardRate(),
                    forward.getForwardPoints(),
                    forward.getDirection(),
                    forward.getSettlementType()
            ));
            writer.newLine();
        }
    }

    private void writeEquityOptions(List<Trade> trades, BufferedWriter writer) throws IOException {
        // Header
        writer.write("tradeId,tradeDate,settlementDate,maturityDate,notional,currency,counterparty,book,trader," +
                "underlyingAsset,optionType,strikePrice,spotPrice,premium,contracts,quantity," +
                "expiryDate,exerciseStyle,moneyness,position,impliedVolatility");
        writer.newLine();

        // Data
        for (Trade trade : trades) {
            EquityOption option = (EquityOption) trade;
            writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    csvEscape(option.getTradeId()),
                    option.getTradeDate(),
                    option.getSettlementDate(),
                    option.getMaturityDate(),
                    option.getNotional(),
                    option.getCurrency(),
                    csvEscape(option.getCounterparty()),
                    csvEscape(option.getBook()),
                    csvEscape(option.getTrader()),
                    csvEscape(option.getUnderlyingAsset()),
                    option.getOptionType(),
                    option.getStrikePrice(),
                    option.getSpotPrice(),
                    option.getPremium(),
                    option.getContracts(),
                    option.getQuantity(),
                    option.getExpiryDate(),
                    option.getExerciseStyle(),
                    option.getMoneyness(),
                    option.getPosition(),
                    option.getImpliedVolatility()
            ));
            writer.newLine();
        }
    }

    private void writeCDS(List<Trade> trades, BufferedWriter writer) throws IOException {
        // Header
        writer.write("tradeId,tradeDate,settlementDate,maturityDate,notional,currency,counterparty,book,trader," +
                "referenceEntity,referenceTicker,sector,creditRating,spreadBps,upfrontPayment," +
                "recoveryRate,paymentFrequency,position,restructuringClause,seniority");
        writer.newLine();

        // Data
        for (Trade trade : trades) {
            CreditDefaultSwap cds = (CreditDefaultSwap) trade;
            writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    csvEscape(cds.getTradeId()),
                    cds.getTradeDate(),
                    cds.getSettlementDate(),
                    cds.getMaturityDate(),
                    cds.getNotional(),
                    cds.getCurrency(),
                    csvEscape(cds.getCounterparty()),
                    csvEscape(cds.getBook()),
                    csvEscape(cds.getTrader()),
                    csvEscape(cds.getReferenceEntity()),
                    csvEscape(cds.getReferenceTicker()),
                    csvEscape(cds.getSector()),
                    csvEscape(cds.getCreditRating()),
                    cds.getSpreadBps(),
                    cds.getUpfrontPayment(),
                    cds.getRecoveryRate(),
                    cds.getPaymentFrequency(),
                    cds.getPosition(),
                    csvEscape(cds.getRestructuringClause()),
                    csvEscape(cds.getSeniority())
            ));
            writer.newLine();
        }
    }

    /**
     * Escape CSV special characters.
     */
    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}