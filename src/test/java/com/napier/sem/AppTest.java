package com.napier.sem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {

    @Test
    void isValidTopN_positive() {
        App app = new App();
        assertTrue(app.isValidTopN(5));
    }

    @Test
    void isValidTopN_zero_false() {
        App app = new App();
        assertFalse(app.isValidTopN(0));
    }

    @Test
    void isValidTopN_negative_false() {
        App app = new App();
        assertFalse(app.isValidTopN(-3));
    }

    @Test
    void formatCountryRow_null_returnsEmpty() {
        App app = new App();
        assertEquals("", app.formatCountryRow(null));
    }

    @Test
    void formatCountryRow_normal_containsFields() {
        App app = new App();
        Country c = new Country();
        c.code = "UKR";
        c.name = "Ukraine";
        c.continent = "Europe";
        c.region = "Eastern Europe";
        c.population = 44000000;
        c.capitalName = "Kyiv";

        String row = app.formatCountryRow(c);
        assertTrue(row.contains("UKR"));
        assertTrue(row.contains("Ukraine"));
        assertTrue(row.contains("Europe"));
        assertTrue(row.contains("Kyiv"));
    }
}

