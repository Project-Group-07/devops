package com.napier.sem;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AppIntegrationTest {

    private App app;

    @BeforeAll
    void init() {
        String host = System.getenv("DB_HOST");
        String delay = System.getenv("DB_DELAY_MS");

        if (host == null || host.isBlank())
            host = "localhost:33060";

        int delayMs = (delay == null || delay.isBlank()) ? 30000 : Integer.parseInt(delay);

        app = new App();
        app.connect(host, delayMs);
    }

    @AfterAll
    void done() {
        app.disconnect();
    }

    // -----------------------------
    // WORLD POPULATION
    // -----------------------------

    @Test
    void world_population_positive() {
        long pop = app.getWorldPopulation();
        assertTrue(pop > 0, "World population must be > 0");
    }

    // -----------------------------
    // ALL COUNTRIES SORTED DESC
    // -----------------------------

    @Test
    void countries_world_sorted_desc() {
        List<Country> list = app.getCountriesWorldByPopulationDesc();
        assertNotNull(list);
        assertTrue(list.size() > 0);

        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).population >= list.get(i).population);
        }
    }

    @Test
    void countries_world_first_is_china() {
        List<Country> list = app.getCountriesWorldByPopulationDesc();
        assertTrue(list.size() > 0);

        Country first = list.get(0);

        assertEquals("CHN", first.code);
        assertEquals("China", first.name);
    }

    // -----------------------------
    // TOP-N CONTINENT
    // -----------------------------

    @Test
    void topN_continent_limit_and_sort() {
        List<Country> list = app.getTopNCountriesInContinent("Europe", 10);
        assertNotNull(list);
        assertTrue(list.size() <= 10);

        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).population >= list.get(i).population);
        }
    }

    @Test
    void topN_continent_zero_returnsEmpty() {
        List<Country> list = app.getTopNCountriesInContinent("Europe", 0);
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    @Test
    void topN_continent_null_returnsEmpty() {
        List<Country> list = app.getTopNCountriesInContinent(null, 5);
        assertNotNull(list);
        assertEquals(0, list.size());
    }
}
