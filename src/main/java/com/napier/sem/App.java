package com.napier.sem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CR3: connection to MySQL `world` database and simple reports + helpers.
 */
public class App {
    /** JDBC connection. */
    private Connection con;

    /**
     * Connect to MySQL. Location e.g. "localhost:33060".
     */
    public void connect(String location, int delayMs) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }
        for (int i = 0; i < 10; i++) {
            System.out.println("Connecting to database...");
            try {
                Thread.sleep(delayMs);
                con = DriverManager.getConnection(
                        "jdbc:mysql://" + location + "/world?allowPublicKeyRetrieval=true&useSSL=false",
                        "root", "example");
                System.out.println("Successfully connected");
                break;
            } catch (Exception e) {
                System.out.println("Failed to connect to database attempt " + i);
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Disconnect from MySQL.
     */
    public void disconnect() {
        if (con != null) {
            try {
                con.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Population of the world (SUM(country.Population)).
     */
    public long getWorldPopulation() {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT SUM(Population) AS pop FROM country")) {
            return rs.next() ? rs.getLong("pop") : 0L;
        } catch (Exception e) {
            System.out.println("getWorldPopulation failed: " + e.getMessage());
            return 0L;
        }
    }

    /**
     * All countries in the world ordered by population (DESC).
     */
    public List<Country> getCountriesWorldByPopulationDesc() {
        List<Country> list = new ArrayList<>();
        String sql = """
                SELECT c.Code, c.Name, c.Continent, c.Region, c.Population, ct.Name AS CapitalName
                FROM country c LEFT JOIN city ct ON c.Capital = ct.ID
                ORDER BY c.Population DESC
                """;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Country c = new Country();
                c.code = rs.getString("Code");
                c.name = rs.getString("Name");
                c.continent = rs.getString("Continent");
                c.region = rs.getString("Region");
                c.population = rs.getLong("Population");
                c.capitalName = rs.getString("CapitalName");
                list.add(c);
            }
        } catch (Exception e) {
            System.out.println("getCountriesWorldByPopulationDesc failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Top-N populated countries in a continent (DESC).
     */
    public List<Country> getTopNCountriesInContinent(String continent, int n) {
        List<Country> list = new ArrayList<>();
        if (!isValidTopN(n) || continent == null || continent.isBlank())
            return list;

        String sql = """
                SELECT c.Code, c.Name, c.Continent, c.Region, c.Population, ct.Name AS CapitalName
                FROM country c LEFT JOIN city ct ON c.Capital = ct.ID
                WHERE c.Continent = ?
                ORDER BY c.Population DESC
                LIMIT ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, continent);
            ps.setInt(2, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Country c = new Country();
                    c.code = rs.getString("Code");
                    c.name = rs.getString("Name");
                    c.continent = rs.getString("Continent");
                    c.region = rs.getString("Region");
                    c.population = rs.getLong("Population");
                    c.capitalName = rs.getString("CapitalName");
                    list.add(c);
                }
            }
        } catch (Exception e) {
            System.out.println("getTopNCountriesInContinent failed: " + e.getMessage());
        }
        return list;
    }

    /** Guard for Top-N. */
    public boolean isValidTopN(int n) {
        return n > 0;
    }

    /**
     * Format country row for console / tests.
     */
    public String formatCountryRow(Country c) {
        if (c == null)
            return "";
        return String.format("%-6s | %-32s | %-13s | %-22s | %12d | %-20s",
                nz(c.code), nz(c.name), nz(c.continent), nz(c.region), c.population, nz(c.capitalName));
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    public static void main(String[] args) {
        App a = new App();

        // Connect to DB in Docker
        if (args.length < 2) {
            a.connect("localhost:33060", 30000);
        } else {
            a.connect(args[0], Integer.parseInt(args[1]));
        }

        // 1) World population
        System.out.println("World population = " + a.getWorldPopulation());

        // 2) Top 10 countries in the world
        List<Country> world = a.getCountriesWorldByPopulationDesc();
        System.out.println("\nTop 10 countries by population (world):");
        for (int i = 0; i < Math.min(10, world.size()); i++) {
            System.out.println(a.formatCountryRow(world.get(i)));
        }

        // 3) Top 5 countries in Europe
        List<Country> europeTop = a.getTopNCountriesInContinent("Europe", 5);
        System.out.println("\nTop 5 countries in Europe:");
        for (Country c : europeTop) {
            System.out.println(a.formatCountryRow(c));
        }

        a.disconnect();
    }
}
