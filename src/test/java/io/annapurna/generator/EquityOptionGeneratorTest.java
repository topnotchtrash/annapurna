package io.annapurna.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.annapurna.generator.EquityOptionGenerator;
import io.annapurna.model.EquityOption;
import io.annapurna.model.Trade;
import io.annapurna.model.TradeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.*;

class EquityOptionGeneratorTest {

    private EquityOptionGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new EquityOptionGenerator();
    }

    @Test
    void testGenerateOneEquityOption() {
        Trade trade = generator.generate();

        assertNotNull(trade);
        assertTrue(trade instanceof EquityOption);
        assertEquals(TradeType.EQUITY_OPTION, trade.getTradeType());

        EquityOption option = (EquityOption) trade;

        // Critical Financial Fields
        assertNotNull(option.getStrikePrice());
        assertNotNull(option.getPremium());
        assertNotNull(option.getImpliedVolatility());
        assertNotNull(option.getMoneyness());

        System.out.println("--- Generated Option ---");
        System.out.printf("Asset: %s | Type: %s | Strike: %s | Spot: %s%n",
                option.getUnderlyingAsset(), option.getOptionType(), option.getStrikePrice(), option.getSpotPrice());
    }

    @Test
    void testMoneynessLogic() {
        // Generate enough trades to hit different moneyness scenarios
        for (int i = 0; i < 50; i++) {
            EquityOption opt = (EquityOption) generator.generate();
            BigDecimal spot = opt.getSpotPrice();
            BigDecimal strike = opt.getStrikePrice();
            String type = opt.getOptionType();
            String moneyness = opt.getMoneyness();

            if ("OTM".equals(moneyness)) {
                if ("CALL".equals(type)) {
                    // OTM Call: Strike > Spot (User would lose money exercising)
                    assertTrue(strike.compareTo(spot) > 0,
                            String.format("OTM Call error: Strike %s should be > Spot %s", strike, spot));
                } else {
                    // OTM Put: Strike < Spot
                    assertTrue(strike.compareTo(spot) < 0,
                            String.format("OTM Put error: Strike %s should be < Spot %s", strike, spot));
                }
            } else if ("ITM".equals(moneyness)) {
                if ("CALL".equals(type)) {
                    // ITM Call: Strike < Spot (User profits exercising)
                    assertTrue(strike.compareTo(spot) < 0,
                            String.format("ITM Call error: Strike %s should be < Spot %s", strike, spot));
                } else {
                    // ITM Put: Strike > Spot
                    assertTrue(strike.compareTo(spot) > 0,
                            String.format("ITM Put error: Strike %s should be > Spot %s", strike, spot));
                }
            }
        }
    }

    @Test
    void testPremiumIntrinsicValue() {
        // Premium must always be >= Intrinsic Value
        for (int i = 0; i < 20; i++) {
            EquityOption opt = (EquityOption) generator.generate();

            double spot = opt.getSpotPrice().doubleValue();
            double strike = opt.getStrikePrice().doubleValue();
            double premium = opt.getPremium().doubleValue();

            double intrinsic = 0;
            if ("CALL".equals(opt.getOptionType())) {
                intrinsic = Math.max(0, spot - strike);
            } else {
                intrinsic = Math.max(0, strike - spot);
            }

            assertTrue(premium >= intrinsic,
                    String.format("Premium %s must be >= Intrinsic Value %s", premium, intrinsic));
        }
    }

    @Test
    void testExpiryIsAlwaysFriday() {
        // Options typically expire on Fridays
        for (int i = 0; i < 20; i++) {
            EquityOption opt = (EquityOption) generator.generate();
            assertEquals(DayOfWeek.FRIDAY, opt.getExpiryDate().getDayOfWeek(),
                    "Expiry date must be a Friday");
        }
    }

    @Test
    void testDateLogic() {
        EquityOption opt = (EquityOption) generator.generate();

        // 1. Maturity (Expiry) > Trade Date
        assertTrue(opt.getMaturityDate().isAfter(opt.getTradeDate()),
                "Maturity/Expiry must be after Trade Date");

        // 2. Settlement Date is T+1 (The day after trade date)
        // Note: Simple check. If Trade Date is Friday, Settlement might be Saturday in simple logic,
        // or Monday in complex holiday logic. Your generator uses strictly plusDays(1).
        assertEquals(opt.getTradeDate().plusDays(1), opt.getSettlementDate(),
                "Settlement must be T+1");
    }

    @Test
    void testRegionBookingLogic() {
        // Verify US stocks are NOT booked in APAC
        for (int i = 0; i < 50; i++) {
            EquityOption opt = (EquityOption) generator.generate();
            String asset = opt.getUnderlyingAsset();
            String book = opt.getBook();

            if ("AAPL".equals(asset) || "MSFT".equals(asset)) {
                assertFalse(book.contains("APAC"),
                        "US Stock " + asset + " should not be booked in APAC: " + book);
            }
        }
    }

    @Test
    void testNotionalCalculation() {
        EquityOption opt = (EquityOption) generator.generate();

        BigDecimal spot = opt.getSpotPrice();
        BigDecimal qty = opt.getQuantity(); // contracts * 100
        BigDecimal notional = opt.getNotional();

        // Verify Notional = Spot * Quantity
        // Use compareTo for BigDecimal safety
        assertEquals(0, notional.compareTo(spot.multiply(qty)),
                "Notional should equal Spot * Quantity");
    }

    @Test
    void testJsonSerialization() throws Exception {
        Trade trade = generator.generate();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String json = mapper.writeValueAsString(trade);

        // Verify JSON fields specific to Options
        assertTrue(json.contains("strikePrice"));
        assertTrue(json.contains("optionType"));
        assertTrue(json.contains("impliedVolatility"));
        assertTrue(json.contains("moneyness"));
    }
}