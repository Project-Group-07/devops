package com.napier.sem;

import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AppIntegrationTest {
    App app;

    @BeforeAll
    void init() {
        String host = System.getenv("DB_HOST");
        String delay = System.getenv("DB_DELAY_MS");
        if (host == null || host.isBlank()) host = "localhost:33060";
        int delayMs = (delay == null || delay.isBlank()) ? 30000 : Integer.parseInt(delay);

        app = new App();
        app.connect(host, delayMs);
    }

    @AfterAll
    void done() { app.disconnect(); }

    @Test
    void world_population_positive() {
        assertTrue(app.getWorldPopulation() > 0);
    }

    @Test
    void countries_world_sorted_desc() {
        List<Country> list = app.getCountriesWorldByPopulationDesc();
        assertNotNull(list);
        assertTrue(list.size() > 0);
        for (int i = 1; i < list.size(); i++)
            assertTrue(list.get(i-1).population >= list.get(i).population);
    }

    @Test
    void topN_continent_limit_and_sort() {
        var list = app.getTopNCountriesInContinent("Europe", 10);
        assertNotNull(list);
        assertTrue(list.size() <= 10);
        for (int i = 1; i < list.size(); i++)
            assertTrue(list.get(i-1).population >= list.get(i).population);
    }
}
