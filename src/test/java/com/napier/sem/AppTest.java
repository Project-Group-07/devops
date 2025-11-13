package com.napier.sem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {

    // -----------------------------
    // isValidTopN() tests
    // -----------------------------

    @Test
    void isValidTopN_positive() {
        App app = new App();
        assertTrue(app.isValidTopN(5));
    }

    @Test
    void isValidTopN_large_positive() {
        App app = new App();
        assertTrue(app.isValidTopN(1000));
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

    // -----------------------------
    // formatCountryRow() tests
    // -----------------------------

    @Test
    void formatCountryRow_null_returnsEmptyString() {
        App app = new App();
        assertEquals("", app.formatCountryRow(null));
    }

    @Test
    void formatCountryRow_normal_containsAllFields() {
        App app = new App();
        Country c = new Country();
        c.code = "UKR";
        c.name = "Ukraine";
        c.continent = "Europe";
        c.region = "Eastern Europe";
        c.population = 44000000L;
        c.capitalName = "Kyiv";

        String row = app.formatCountryRow(c);

        assertTrue(row.contains("UKR"));
        assertTrue(row.contains("Ukraine"));
        assertTrue(row.contains("Europe"));
        assertTrue(row.contains("Eastern Europe"));
        assertTrue(row.contains("44000000"));
        assertTrue(row.contains("Kyiv"));
    }

    @Test
    void formatCountryRow_handlesNullFieldsInsideCountry() {
        App app = new App();
        Country c = new Country();

        // Only name is non-null
        c.name = "Testland";
        c.population = 123;

        String row = app.formatCountryRow(c);

        assertTrue(row.contains("Testland"));
        assertTrue(row.contains("123"));
        // Should not crash
    }

    @Test
    void formatCountryRow_formatStructure_hasColumns() {
        App app = new App();
        Country c = new Country();
        c.code = "ABC";
        c.name = "Test";
        c.continent = "TestCont";
        c.region = "Region";
        c.population = 9999;
        c.capitalName = "Capital";

        String row = app.formatCountryRow(c);

        // Expect at least 6 columns separated by "|"
        String[] parts = row.split("\\|");
        assertTrue(parts.length >= 6);
    }
}


