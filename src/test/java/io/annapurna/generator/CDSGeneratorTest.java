package io.annapurna.generator;

import io.annapurna.generator.CDSGenerator;
import io.annapurna.model.CreditDefaultSwap;
import io.annapurna.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CDSGeneratorTest {

    private CDSGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new CDSGenerator();
    }

    @Test
    void testGenerateOneCDS() {
        CreditDefaultSwap cds = (CreditDefaultSwap) generator.generate();

        assertNotNull(cds.getReferenceEntity());
        assertNotNull(cds.getCreditRating());
        assertNotNull(cds.getSpreadBps());

        System.out.printf("Entity: %s [%s] | Spread: %d bps | Book: %s%n",
                cds.getReferenceEntity(), cds.getCreditRating(), cds.getSpreadBps(), cds.getBook());
    }

    @Test
    void testBookingLogic() {
        // IG entities must be in IG books, HY in HY books
        for (int i = 0; i < 50; i++) {
            CreditDefaultSwap cds = (CreditDefaultSwap) generator.generate();
            String rating = cds.getCreditRating();
            String book = cds.getBook();

            if (rating.startsWith("A") || rating.startsWith("BBB")) {
                assertTrue(book.contains("IG") || book.contains("EM"),
                        "IG Rating " + rating + " should not be in HY Book: " + book);
            } else if (rating.startsWith("C")) {
                assertTrue(book.contains("HY") || book.contains("EM"),
                        "Distressed Rating " + rating + " should not be in IG Book: " + book);
            }
        }
    }

    @Test
    void testUpfrontPaymentLogic() {
        // Distressed entities (C rated) usually have upfront payments
        boolean foundDistressed = false;

        for (int i = 0; i < 100; i++) {
            CreditDefaultSwap cds = (CreditDefaultSwap) generator.generate();
            if (cds.getCreditRating().startsWith("C")) {
                foundDistressed = true;
                // Check that upfront is positive
                assertTrue(cds.getUpfrontPayment().compareTo(BigDecimal.ZERO) > 0,
                        "Distressed entity " + cds.getReferenceEntity() + " should have upfront payment");
            }
        }
    }

    @Test
    void testRecoveryRateLogic() {
        // Senior Unsecured should usually be 40% (Standard)
        for (int i = 0; i < 20; i++) {
            CreditDefaultSwap cds = (CreditDefaultSwap) generator.generate();
            if ("SENIOR_UNSECURED".equals(cds.getSeniority()) && !"SOVEREIGN".equals(cds.getSector())) {
                assertEquals(0, cds.getRecoveryRate().compareTo(BigDecimal.valueOf(40)),
                        "Standard Corporate Senior Unsecured should have 40% recovery");
            }
        }
    }
}