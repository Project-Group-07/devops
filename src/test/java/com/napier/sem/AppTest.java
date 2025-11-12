package com.napier.sem;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AppTest {
    @Test
    void topN_guard() {
        App a = new App();
        assertFalse(a.isValidTopN(0));
        assertFalse(a.isValidTopN(-3));
        assertTrue(a.isValidTopN(5));
    }

    @Test
    void formatCountryRow_ok() {
        App a = new App();
        Country c = new Country();
        c.code = "GBR";
        c.name = "United Kingdom";
        c.continent = "Europe";
        c.region = "British Islands";
        c.population = 60000000L;
        c.capitalName = "London";
        String row = a.formatCountryRow(c);
        assertNotNull(row);
        assertTrue(row.contains("United Kingdom"));
        assertTrue(row.contains("60000000"));
    }

    @Test
    void formatCountryRow_nullSafe() {
        App a = new App();
        assertEquals("", a.formatCountryRow(null));
    }
}
